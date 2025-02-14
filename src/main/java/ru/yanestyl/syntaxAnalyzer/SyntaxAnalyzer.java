package ru.yanestyl.syntaxAnalyzer;

import ru.yanestyl.lexicalAnalizer.Token;

import java.util.*;

public class SyntaxAnalyzer {

    private List<Token> tokens;
    private int currentTokenIndex;
    private Token currentToken;
    private Set<String> declaredVariables; // Хранит переменные, которые были объявлены
    private Set<String> initializedVariables; // Хранит переменные, которые были инициализированы

    public SyntaxAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
        this.currentTokenIndex = 0;
        this.currentToken = tokens.get(currentTokenIndex);
        this.declaredVariables = new HashSet<>();
        this.initializedVariables = new HashSet<>();
    }

    // Метод для получения следующей лексемы
    private void advance() {
        currentTokenIndex++;
        if (currentTokenIndex < tokens.size()) {
            currentToken = tokens.get(currentTokenIndex);
        } else {
            currentToken = null;  // Конец списка лексем
        }
    }

    // Метод для синтаксического анализа программы
    public void parse() throws SyntaxException {
        program();
    }

    private void program() throws SyntaxException {
        int leftSquareCount = 0; // Счетчик для '['
        int rightSquareCount = 0; // Счетчик для ']'

        // Программа должна начинаться с 'Var' или с '[', иначе ошибка
        if (currentToken.getType().equals("Var")) {
            statement(); // Обработка объявления переменных
        }

        if (!currentToken.getType().equals("LEFT_SQUARE")) {
            throw new SyntaxException("Программа должна начинаться с '[' на строке " + currentToken.getLine());
        }
        leftSquareCount++;
        advance();

        while (currentToken != null && !currentToken.getType().equals("RIGHT_SQUARE")) {
            if (currentToken.getType().equals("LEFT_SQUARE")) {
                throw new SyntaxException("Ошибка: '[' может встречаться только один раз в начале программы.");
            }
            if (currentToken.getType().equals("RIGHT_SQUARE")) {
                throw new SyntaxException("Ошибка: ']' встретился до конца программы.");
            }
            statement(); // Анализируем оставшиеся операторы
        }

        if (currentToken == null || !currentToken.getType().equals("RIGHT_SQUARE")) {
            throw new SyntaxException("Программа должна заканчиваться ']' на строке " + (currentToken != null ? currentToken.getLine() : "конце файла"));
        }
        rightSquareCount++;
        advance();

        // После закрывающей скобки ']' не должно быть других операторов
        if (currentToken != null) {
            throw new SyntaxException("Ошибка: после ']' не должно быть кода. Найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }

        // Проверяем, что квадратные скобки корректны
        if (leftSquareCount != 1 || rightSquareCount != 1) {
            throw new SyntaxException("Квадратные скобки должны встречаться только один раз в начале и один раз в конце программы.");
        }
    }



    private void statement() throws SyntaxException {
        if (currentToken.getType().equals("Var")) {
            variableDeclaration();
        } else if (currentToken.getType().equals("IDENTIFIER")) {
            assignment();
        } else {
            throw new SyntaxException("Ожидалась строка с 'Var' или присваиванием. Найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
    }
    private void variableDeclaration() throws SyntaxException {
        if (!currentToken.getType().equals("Var")) {
            throw new SyntaxException("Ожидалось ключевое слово 'Var', найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
        advance();  // Пропускаем 'Var'

        // Проверка на null после advance()
        if (currentToken == null) {
            throw new SyntaxException("Неожиданный конец файла после 'Var'.");
        }

        while (currentToken != null && currentToken.getType().equals("IDENTIFIER")) {
            declaredVariables.add(currentToken.getValue());  // Добавляем переменную в список объявленных
            advance();  // Пропускаем идентификатор

            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла при обработке переменных.");
            }

            if (currentToken != null && currentToken.getType().equals("COMMA")) {
                advance();  // Пропускаем запятую, если есть несколько переменных
            }

            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла после запятой.");
            }
        }

        if (currentToken != null && !currentToken.getType().equals("SEMICOLON")) {
            throw new SyntaxException("Ожидалась точка с запятой, найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
        advance();  // Пропускаем точку с запятой


    }

    private void assignment() throws SyntaxException {
        if (!currentToken.getType().equals("IDENTIFIER")) {
            throw new SyntaxException("Ожидался идентификатор, найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
        String variableName = currentToken.getValue();  // Запоминаем имя переменной
        if (!declaredVariables.contains(variableName)) {
            throw new SyntaxException("Использована неинициализированная переменная: " + variableName + " на строке " + currentToken.getLine());
        }
        advance();  // Пропускаем идентификатор

        // Проверка на null после advance()
        if (currentToken == null) {
            throw new SyntaxException("Неожиданный конец файла после идентификатора.");
        }

        if (!currentToken.getType().equals("OPERATOR")) {
            throw new SyntaxException("Ожидался знак присваивания '=', найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
        advance();  // Пропускаем знак присваивания

        // Проверка на null после advance()
        if (currentToken == null) {
            throw new SyntaxException("Неожиданный конец файла после знака присваивания.");
        }

        expression();  // Анализируем выражение
        initializedVariables.add(variableName);  // Помечаем переменную как инициализированную

        if (!currentToken.getType().equals("SEMICOLON")) {
            throw new SyntaxException("Ожидалась точка с запятой, найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
        advance();  // Пропускаем точку с запятой

    }

    private void expression() throws SyntaxException {
        term();
        while (currentToken != null && (currentToken.getType().equals("OPERATOR") && (currentToken.getValue().equals("+") || currentToken.getValue().equals("-")))) {
            String operator = currentToken.getValue();  // Сохраняем текущий оператор
            advance();  // Пропускаем текущий оператор

            // Проверка на следующий оператор
            if (currentToken != null && currentToken.getType().equals("OPERATOR") && (currentToken.getValue().equals("+") || currentToken.getValue().equals("-"))) {
                throw new SyntaxException("Ошибка: два оператора подряд: " + operator + currentToken.getValue() + " на строке " + currentToken.getLine());
            }

            term();  // Обрабатываем следующее выражение
        }
    }


    private void term() throws SyntaxException {
        factor();
        while (currentToken != null && (currentToken.getType().equals("OPERATOR") && (currentToken.getValue().equals("*") || currentToken.getValue().equals("/")))) {
            advance();  // Пропускаем операцию
            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла в терме.");
            }
            factor();
        }
    }

    private void factor() throws SyntaxException {
        if (currentToken.getType().equals("OPERATOR") && currentToken.getValue().equals("-")) {
            // Обрабатываем унарный минус
            advance();  // Пропускаем минус
            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла после унарного минуса.");
            }
            factor();  // Обрабатываем фактор после унарного минуса
        } else if (currentToken.getType().equals("NUMBER") || currentToken.getType().equals("IDENTIFIER")) {
            String varName = currentToken.getValue();
            if (currentToken.getType().equals("IDENTIFIER") && !initializedVariables.contains(varName)) {
                throw new SyntaxException("Использована неинициализированная переменная: " + varName + " на строке " + currentToken.getLine());
            }
            advance();  // Пропускаем число или идентификатор

            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла после числа или идентификатора.");
            }
        } else if (currentToken.getType().equals("LEFT_PAREN")) {
            advance();  // Пропускаем открытую скобку
            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла после открывающей скобки.");
            }
            expression();
            if (!currentToken.getType().equals("RIGHT_PAREN")) {
                throw new SyntaxException("Ожидалась правая скобка, найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
            }
            advance();  // Пропускаем закрытую скобку

            // Проверка на null после advance()
            if (currentToken == null) {
                throw new SyntaxException("Неожиданный конец файла после закрывающей скобки.");
            }
        } else {
            throw new SyntaxException("Ожидался фактор, найдено: " + currentToken.getValue() + " на строке " + currentToken.getLine());
        }
    }
}
