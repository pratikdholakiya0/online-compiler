package com.app.babycode.service;

import com.app.babycode.dto.ExecutionResult;
import com.app.babycode.dto.LanguageConfig;
import com.app.babycode.dto.SubmissionRequest;
import com.app.babycode.entity.Submission;
import com.app.babycode.enums.Language;
import com.app.babycode.enums.SubmissionStatus;
import com.app.babycode.enums.Verdict;
import com.app.babycode.handler.custom.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class CodeExecutionService {
    @Value("${app.workspace-dir}")
    private String workspaceDir;

    @Value("${app.workspace-host-dir}")
    private String workspaceHostDir;

    private final ExecutorService pool = Executors.newFixedThreadPool(4);
    private static final ConcurrentHashMap<UUID, Submission> map = new ConcurrentHashMap<>();
    private final Map<Language, LanguageConfig> languageConfig;

    public CodeExecutionService(@Qualifier("languageConfig") Map<Language, LanguageConfig> languageConfig){
        this.languageConfig = languageConfig;
    }

    private String hostMountPath(Path workDir) {
        String folderName = workDir.getFileName().toString();
        if (workspaceHostDir == null || workspaceHostDir.isBlank()) {
            return workDir.toAbsolutePath().toString();
        }
        return workspaceHostDir + "//" + folderName;
    }

    public Submission submitCode(SubmissionRequest request) throws Exception {
        UUID submissionId = UUID.randomUUID();
        Submission submission = response(submissionId, null, request.getLanguage(), SubmissionStatus.QUEUED,request.getSourceCode(), request.getStdIn());

        map.put(submissionId, submission);

        CompletableFuture.supplyAsync(()-> {
            try {
                return executeCode(submissionId, request);
            } catch (Exception e) {
                submission.setResult(executionResult(Verdict.INTERNAL_ERROR, null, "Internal error: " + e.getMessage()));
                submission.setStatus(SubmissionStatus.COMPLETED);
                log.warn("Code execution failed");
            }
            return null;
        }, pool);

        return submission;
    }

    public Submission getSubmission(UUID submissionId){
        Submission submission = map.get(submissionId);
        if (submission==null) throw new ResourceNotFoundException("Submission not found!");
        return submission;
    }

    public ExecutionResult executeCode(UUID submissionId, SubmissionRequest request) throws Exception {
        com.app.babycode.dto.LanguageConfig config = languageConfig.get(request.getLanguage());

        Submission submission = getSubmission(submissionId);
        submission.setStatus(SubmissionStatus.RUNNING);

        Path workDir = Files.createTempDirectory(Path.of(workspaceDir), "submission-");
        File file = workDir.resolve(config.fileName()).toFile();
        file.deleteOnExit();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(request.getSourceCode());
        }

        if (config.compileCommand()!=null){
            ExecutionResult compileResult = compile(request, workDir);
            if (compileResult.getStatus()!=Verdict.SUCCESS){
                submission.setResult(compileResult);
                submission.setStatus(SubmissionStatus.COMPLETED);
                return compileResult;
            }
        }

        String containerName = UUID.randomUUID().toString();
        String[] runParts = config.runCommand().split(" ");

        List<String> commandParts = new ArrayList<>(List.of(
                "docker", "run", "--rm", "-i",
                "--name=" + containerName,
                "--memory=256m", "--memory-swap=256m",
                "--read-only", "--tmpfs", "/tmp:size=64m",
                "--cpus=1",
                "--pids-limit=64",
                "--network=none",
                "-v", hostMountPath(workDir) + ":/workspace",
                config.image()
        ));
        commandParts.addAll(Arrays.asList(runParts));

        ProcessBuilder pb = new ProcessBuilder(commandParts);

        Process process = pb.start();

        try(OutputStream stdin = process.getOutputStream()){
            stdin.write(request.getStdIn().getBytes());
            stdin.flush();
        } catch (IOException e) {
            log.debug("stdin write failed (process likely didn't read input): {}", e.getMessage());
        }

        Future<String> stdoutFuture = pool.submit(() -> readAll(process.getInputStream(), 64* 1024));
        Future<String> stderrFuture = pool.submit(() -> readAll(process.getErrorStream(), 64 * 1024));

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            try{
                new ProcessBuilder("docker", "kill", containerName).start().waitFor();
                submission.setStatus(SubmissionStatus.COMPLETED);
            } catch (Exception e) {
                log.warn("Failed to kill container after timeout: {}", e.getMessage());
            }

            String partialStdout = stdoutFuture.get();
            String partialStderr = stderrFuture.get();

            process.destroyForcibly();
            return executionResult(Verdict.TIME_LIMIT_EXCEEDED, partialStdout, partialStderr);
        }

        int exitCode = process.exitValue();
        String stdout = stdoutFuture.get();
        String stderr = stderrFuture.get();

        System.out.println(Files.readString(file.toPath()));
        String errorResponse = stderr.replace(file.getAbsolutePath(), file.getName());

        process.destroyForcibly();

        ExecutionResult executionResult = executionResult(exitCode == 0 ? Verdict.SUCCESS : exitCode==137? Verdict.MEMORY_LIMIT_EXCEEDED :  Verdict.RUNTIME_ERROR, stdout.replace("\n", ""), errorResponse);

        submission.setResult(executionResult);
        submission.setStatus(SubmissionStatus.COMPLETED);

        return executionResult;
    }

    public ExecutionResult compile(SubmissionRequest request, Path workDir) throws Exception {
        LanguageConfig config = languageConfig.get(request.getLanguage());

        String containerName = UUID.randomUUID().toString();

        List<String> commands = new ArrayList<>(List.of(
                "docker", "run", "--rm",
                "--name=" + containerName,
                "--memory=256m", "--memory-swap=256m",
                "--read-only", "--tmpfs", "/tmp:size=64m",
                "--cpus=1",
                "--pids-limit=64",
                "--network=none",
                "-v", hostMountPath(workDir) + ":/workspace",
                config.image()
        ));
        commands.addAll(Arrays.asList(config.compileCommand().split(" ")));

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        Process process = processBuilder.start();

        Future<String> stderr = pool.submit(()-> readAll(process.getErrorStream(), 64 * 1024));

        boolean finished = process.waitFor(5, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            try {
                new ProcessBuilder("docker", "kill", containerName).start().waitFor();
            } catch (Exception e) {
                log.warn("Docker kill failed!");
            }
            return executionResult(Verdict.TIME_LIMIT_EXCEEDED, null, stderr.get());
        }
        int exitCode = process.exitValue();
        if (exitCode!=0) return executionResult(Verdict.COMPILE_TIME_ERROR, null, stderr.get());
        return executionResult(Verdict.SUCCESS, null, null);
    }

    public String readAll(InputStream in, int max) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];

        int charsRead;
        while ((charsRead = br.read(buffer)) != -1) {
            sb.append(buffer, 0, charsRead);

            if (sb.length() >= max) {
                sb.append("\n... [output truncated, exceeded ")
                        .append(max / 1024)
                        .append("KB]");
                break;
            }
        }
        return sb.toString().replace("\u0000", "");
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void clean(){
        map.entrySet().removeIf(entry ->
                entry.getValue().getSubmittedAt().plusMinutes(10).isBefore(LocalDateTime.now())
        );
    }

    private Submission response(UUID submissionId, ExecutionResult executionResult, Language language, SubmissionStatus status, String sourceCode, String stdIn){
        return Submission.builder()
                .id(submissionId)
                .language(language)
                .sourceCode(sourceCode)
                .stdin(stdIn)
                .result(executionResult)
                .status(status)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    private ExecutionResult executionResult(Verdict status, String output, String error){
        return ExecutionResult.builder()
                .status(status)
                .output(output)
                .error(error)
                .build();
    }
}
