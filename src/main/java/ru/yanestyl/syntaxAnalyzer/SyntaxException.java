package ru.yanestyl.syntaxAnalyzer;

// Исключение для синтаксических ошибок
public class SyntaxException extends Exception {
    public SyntaxException(String message) {
        super(message);
    }
}
