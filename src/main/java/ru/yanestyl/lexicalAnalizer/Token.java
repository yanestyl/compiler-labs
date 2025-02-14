package ru.yanestyl.lexicalAnalizer;

import lombok.Data;
import lombok.Getter;


@Data
// Класс для представления лексемы
public class Token {
    private final String type;
    private final String value;
    private final int line;

    public Token(String type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() {
        return "(" + type + ": " + value + ", строка " + line + ")";
    }
}