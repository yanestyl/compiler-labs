package ru.yanestyl.postfix;

import java.util.*;

public class PostfixGenerator {

    public static StringBuilder convertCodeToPostfix(String code) {
        StringBuilder postfix = new StringBuilder();
        String[] lines = code.split(("[\\[\\];\n]")); // Разделяем код по строкам (по символу ;)

        for (String line : lines) {
            line = line.trim(); // Убираем пробелы по краям
            if (line.isEmpty()) continue; // Пропускаем пустые строки

            if (line.startsWith("Var")) {
                // Игнорируем строку объявления переменных
                continue;
            } else if (line.contains("=")) {
                // Обрабатываем строку присваивания
                String[] parts = line.split("=");
                String variable = parts[0].trim(); // Левая часть (переменная)
                String expression = parts[1].trim(); // Правая часть (выражение)

                // Преобразуем выражение в постфиксную запись
                String postfixExpression = convertToPostfix(expression);

                // Формируем результат с пробелами между элементами
                postfix.append(postfixExpression).append(" $ ").append(variable).append(" ");
            }
        }

        return postfix;
    }

    // Преобразование инфиксного выражения в постфиксное
    private static String convertToPostfix(String expression) {
        Stack<Character> operatorStack = new Stack<>();
        StringBuilder postfix = new StringBuilder();
        boolean expectOperand = true; // Для отслеживания, ожидаем ли операнд или оператор
        StringBuilder numberBuffer = new StringBuilder(); // Для сбора чисел

        for (int i = 0; i < expression.length(); i++) {
            char current = expression.charAt(i);

            if (Character.isDigit(current)) {
                // Собираем число
                numberBuffer.append(current);
                expectOperand = false; // После числа ожидаем оператор
            } else {
                if (numberBuffer.length() > 0) {
                    // Если в буфере есть число, добавляем его в постфиксное выражение
                    postfix.append(numberBuffer).append(' ');
                    numberBuffer.setLength(0); // Очищаем буфер
                }

                if (Character.isLetter(current)) {
                    // Если текущий символ - буква (переменная), добавляем в постфиксное выражение
                    postfix.append(current).append(' ');
                    expectOperand = false;

                } else if (current == '(') {
                    operatorStack.push(current);
                    expectOperand = true; // После открывающей скобки ожидаем операнд
                } else if (current == ')') {
                    while (!operatorStack.isEmpty() && operatorStack.peek() != '(') {
                        postfix.append(operatorStack.pop()).append(' ');
                    }
                    operatorStack.pop(); // Убираем '(' из стека
                    expectOperand = false; // После закрывающей скобки ожидаем оператор
                } else if (isOperator(current)) {
                    // Если текущий символ - оператор
                    if (expectOperand && current == '-') {
                        // Проверяем, нужен ли ноль перед унарным минусом
                        if (i == 0 || expression.charAt(i - 1) == '(') {
                            postfix.append("0 ");
                        }
                        operatorStack.push('~'); // Используем специальный символ для унарного минуса
                    } else {
                        while (!operatorStack.isEmpty() &&
                                precedence(operatorStack.peek()) >= precedence(current)) {
                            postfix.append(operatorStack.pop()).append(' ');
                        }
                        operatorStack.push(current);
                    }
                    expectOperand = true; // После оператора ожидаем операнд
                }
            }
        }

        // Добавляем оставшееся число из буфера
        if (numberBuffer.length() > 0) {
            postfix.append(numberBuffer).append(' ');
        }

        // Добавляем оставшиеся операторы из стека
        while (!operatorStack.isEmpty()) {
            postfix.append(operatorStack.pop()).append(' ');
        }

        // Заменяем символ унарного минуса '~' на '-'
        return postfix.toString().replace("~", "-").trim();
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '~';
    }

    private static int precedence(char operator) {
        switch (operator) {
            case '~': // Унарный минус
                return 3;
            case '*':
            case '/':
                return 2;
            case '+':
            case '-':
                return 1;
            default:
                return -1;
        }
    }
}
