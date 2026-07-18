package com.app.babycode.config;

import com.app.babycode.dto.LanguageConfig;
import com.app.babycode.enums.Language;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Config {
    @Bean(name = "languageConfig")
    public Map<Language, LanguageConfig> languageMap() {
        Map<Language, LanguageConfig> map = new HashMap<>();

        map.put(Language.PYTHON, new LanguageConfig(
                null,
                "python /workspace/script.py",
                "python:3.12-slim",
                "script.py"
        ));

        map.put(Language.JAVASCRIPT, new LanguageConfig(
                null,
                "node /workspace/script.js",
                "node:20-slim",
                "script.js"
        ));

        map.put(Language.CPP, new LanguageConfig(
                "g++ /workspace/script.cpp -o /workspace/output",
                "/workspace/output",
                "gcc:13",
                "script.cpp"
        ));

        map.put(Language.JAVA, new LanguageConfig(
                "javac /workspace/Main.java -d /workspace",
                "java -cp /workspace Main",
                "eclipse-temurin:17-jdk-alpine",
                "Main.java"
        ));

        map.put(Language.C, new LanguageConfig(
                "g++ /workspace/script.c -o /workspace/output",
                "/workspace/output",
                "gcc:13",
                "script.c"
        ));
        return map;
    }
}
