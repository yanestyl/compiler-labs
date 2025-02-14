package ru.yanestyl.lexicalAnalizer;

import java.util.*;
import java.util.regex.Pattern;

public class LexicalAnalyzer {

    // Регулярные выражения для лексем
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9]*");
    private static final Pattern CONSTANT_PATTERN = Pattern.compile("\\d+");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("[+\\-*/]");
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile("=");  // Присваивание
    private static final Pattern VAR_PATTERN = Pattern.compile("Var");  // Ключевое слово Var
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");  // Запятая
    private static final Pattern SEMICOLON_PATTERN = Pattern.compile(";");  // Точка с запятой
    private static final Pattern LPAREN_PATTERN = Pattern.compile("\\(");  // Левая скобка
    private static final Pattern RPAREN_PATTERN = Pattern.compile("\\)");  // Правая скобка
    private static final Pattern LSQUARE_PATTERN = Pattern.compile("\\["); // Открывающая квадратная скобка
    private static final Pattern RSQUARE_PATTERN = Pattern.compile("\\]"); // Закрывающая квадратная скобка

    public static List<Token> analyze(String code) throws LexicalException {
        List<Token> tokens = new ArrayList<>();
        int position = 0;
        int lineNumber = 1;  // Номер строки, начинаем с первой

        // Проходим по всем символам исходного кода
        while (position < code.length()) {
            char currentChar = code.charAt(position);

            // Если символ новой строки, увеличиваем номер строки
            if (currentChar == '\n') {
                lineNumber++;
                position++;
                continue;
            }

            // Игнорируем пробелы
            if (Character.isWhitespace(currentChar)) {
                position++;
                continue;
            }

            // Обработка квадратных скобок
            if (currentChar == '[') {
                tokens.add(new Token("LEFT_SQUARE", "[", lineNumber));
                position++;
                continue;
            } else if (currentChar == ']') {
                tokens.add(new Token("RIGHT_SQUARE", "]", lineNumber));
                position++;
                continue;
            }

            // Обработка ключевого слова Var
            if (position + 3 <= code.length() && code.substring(position, position + 3).equals("Var")) {
                tokens.add(new Token("Var", "Var", lineNumber));
                position += 3;
                continue;
            }

            // Обработка идентификатора (переменной)
            if (Character.isLetter(currentChar)) {
                StringBuilder id = new StringBuilder();
                while (position < code.length() && (Character.isLetterOrDigit(code.charAt(position)))) {
                    id.append(code.charAt(position));
                    position++;
                }
                tokens.add(new Token("IDENTIFIER", id.toString(), lineNumber));
            }
            // Обработка числовой константы
            else if (Character.isDigit(currentChar)) {
                StringBuilder number = new StringBuilder();
                while (position < code.length() && Character.isDigit(code.charAt(position))) {
                    number.append(code.charAt(position));
                    position++;
                }
                tokens.add(new Token("NUMBER", number.toString(), lineNumber));
            }
            // Обработка запятой
            else if (currentChar == ',') {
                tokens.add(new Token("COMMA", ",", lineNumber));
                position++;
            }
            // Обработка точки с запятой
            else if (currentChar == ';') {
                tokens.add(new Token("SEMICOLON", ";", lineNumber));
                position++;
            }
            // Обработка знака присваивания "="
            else if (currentChar == '=') {
                tokens.add(new Token("OPERATOR", "=", lineNumber));
                position++;
            }
            // Обработка операций: +, -, *, /
            else if ("+-*/".indexOf(currentChar) != -1) {
                tokens.add(new Token("OPERATOR", String.valueOf(currentChar), lineNumber));
                position++;
            }
            // Обработка скобок
            else if (currentChar == '(') {
                tokens.add(new Token("LEFT_PAREN", "(", lineNumber));
                position++;
            } else if (currentChar == ')') {
                tokens.add(new Token("RIGHT_PAREN", ")", lineNumber));
                position++;
            }
            // Если встретили некорректный символ, выбрасываем ошибку
            else {
                throw new LexicalException("Некорректный символ: '" + currentChar + "' в позиции " + position + " на строке " + lineNumber);
            }
        }

        return tokens;
    }
}
