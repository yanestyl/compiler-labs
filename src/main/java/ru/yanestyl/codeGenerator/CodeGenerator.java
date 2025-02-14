package ru.yanestyl.codeGenerator;

import java.util.*;

public class CodeGenerator {
    // Список команд, которые будет генерировать программа
    private List<String> program = new ArrayList<>();
    // Список переменных (по порядку их индексов)
    private Map<String, Integer> variables = new HashMap<>();
    private int variableCount = 0;

    // Метод для добавления команды в программу
    private void addCommand(String command) {
        program.add(command);
    }

    // Метод для получения id переменной или добавления новой переменной
    private int getVariableId(String var) {
        if (!variables.containsKey(var)) {
            variables.put(var, variableCount++);
        }
        return variables.get(var);
    }

    // Метод для преобразования постфиксной записи в команды виртуальной машины
    public String generateMnemonics(String postfix) {
        String[] strings = postfix.split(" ");
        ArrayList<String> tokens = new ArrayList<>(Arrays.asList(strings));
        for (int i = 0; i < strings.length; i++) {
            if (tokens.get(i).equals(" ") || tokens.get(i).isEmpty()) {
                tokens.set(i, "null");
            }
        }
        // Пройдем по всем токенам из постфиксной записи
        for (int i = 0; i < tokens.size(); i++) {
            // Если это число (константа), добавляем команду LIT
            try {
                int constValue = Integer.parseInt((tokens.get(i)));
                addCommand("LIT " + constValue);
            } catch (NumberFormatException e) {
                // Если это оператор или переменная
                switch (tokens.get(i)) {
                    case "+":
                        addCommand("ADD");
                        break;
                    case "-":
                        addCommand("SUB");
                        break;
                    case "*":
                        addCommand("MUL");
                        break;
                    case "/":
                        addCommand("DIV");
                        break;
                    case "AND":
                        addCommand("AND");
                        break;
                    case "OR":
                        addCommand("OR");
                        break;
                    case "XOR":
                        addCommand("XOR");
                        break;
                    case "NOT":
                        addCommand("NOT");
                        break;
                    case "$":
                        int varToStoreId = getVariableId(tokens.get(i+1)); // Следующий токен - переменная для записи
                        addCommand("STO " + varToStoreId);
                        i++;
                        break;
                    default:
                        if (!Objects.equals(String.valueOf(tokens.get(i)), "null"))
                        {
                            int varId = getVariableId((tokens.get(i)));
                            addCommand("LOAD " + varId);  // Загружаем значение переменной в стек
                        }



                            break;
                }
            }
        }

        // Сформируем строки команд из накопленных в стеке значений
        StringBuilder stringBuilder = new StringBuilder();
        for (String command : program) {
            stringBuilder.append(command).append("\n");
        }

        return stringBuilder.toString();
    }


}
