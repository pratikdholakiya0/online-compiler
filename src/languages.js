export const LANGUAGES = [
  {
    value: 'PYTHON',
    label: 'Python',
    monacoId: 'python',
    starter: 'name = input()\nprint(f"Hello, {name}!")\n'
  },
  {
    value: 'JAVASCRIPT',
    label: 'JavaScript',
    monacoId: 'javascript',
    starter: 'console.log("Hello from JS!");\n'
  },
  {
    value: 'CPP',
    label: 'C++',
    monacoId: 'cpp',
    starter:
      '#include <iostream>\n\nint main() {\n    std::cout << "Hello from C++!" << std::endl;\n    return 0;\n}\n'
  },
  {
    value: 'JAVA',
    label: 'Java',
    monacoId: 'java',
    starter:
      'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello from Java!");\n    }\n}\n'
  }
];
