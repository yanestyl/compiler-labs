package ru.yanestyl;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Objects;

import ru.yanestyl.codeGenerator.CodeGenerator;
import ru.yanestyl.syntaxAnalyzer.SyntaxException;
import ru.yanestyl.useFile.SaveToFile;
import ru.yanestyl.syntaxAnalyzer.SyntaxAnalyzer;
import ru.yanestyl.lexicalAnalizer.LexicalAnalyzer;
import ru.yanestyl.lexicalAnalizer.LexicalException;
import ru.yanestyl.lexicalAnalizer.Token;
import ru.yanestyl.postfix.PostfixGenerator;

public class Main {

    private static JTextArea codeInputArea;
    private static JTextArea resultOutputArea;

    public static void main(String[] args) {
        // Создаем окно
        JFrame frame = new JFrame("Лексический Анализатор");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Панель выбора режима работы
        JPanel modePanel = new JPanel();
        modePanel.setLayout(new FlowLayout());

        // Выпадающий список для выбора режима
        String[] modes = {"Лексический анализатор", "Синтаксический анализатор", "Генератор постфикса", "Генерация кода"};
        JComboBox<String> modeComboBox = new JComboBox<>(modes);
        modePanel.add(new JLabel("Выберите режим работы:"));
        modePanel.add(modeComboBox);

        // Панель для кнопок
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        // Кнопка загрузки файла
        JButton loadFileButton = new JButton("Загрузить файл");
        loadFileButton.addActionListener(e -> loadFile());
        buttonPanel.add(loadFileButton);

        // Кнопка анализа
        JButton analyzeButton = new JButton("Анализировать");
        analyzeButton.addActionListener(e -> analyzeCode(Objects.requireNonNull(modeComboBox.getSelectedItem()).toString()));
        buttonPanel.add(analyzeButton);

        // Панель для ввода кода (слева)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        codeInputArea = new JTextArea();
        codeInputArea.setLineWrap(true);
        codeInputArea.setWrapStyleWord(true);
        codeInputArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        inputPanel.add(new JScrollPane(codeInputArea), BorderLayout.CENTER);
        inputPanel.setPreferredSize(new Dimension(350, 400));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Ввод кода"));

        // Панель для вывода результата (справа)
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout());
        resultOutputArea = new JTextArea();
        resultOutputArea.setEditable(false);
        resultOutputArea.setLineWrap(true);
        resultOutputArea.setWrapStyleWord(true);
        resultPanel.add(new JScrollPane(resultOutputArea), BorderLayout.CENTER);
        resultPanel.setPreferredSize(new Dimension(350, 400));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Результат"));

        // Добавление элементов на основное окно
        frame.add(modePanel, BorderLayout.NORTH);
        frame.add(inputPanel, BorderLayout.WEST);
        frame.add(resultPanel, BorderLayout.EAST);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Отображение окна
        frame.setVisible(true);
    }

    // Метод для загрузки файла
    private static void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                codeInputArea.setText("");
                String line;
                while ((line = reader.readLine()) != null) {
                    codeInputArea.append(line + "\n");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Ошибка при загрузке файла: " + e.getMessage());
            }
        }
    }
    // Метод для анализа кода
    private static void analyzeCode(String selectedMode) {
        String code = codeInputArea.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Пожалуйста, введите код для анализа.");
            return;
        }

        try {
            resultOutputArea.setText(""); // Очищаем поле результата

            if ("Лексический анализатор".equals(selectedMode)) {
                // Лексический анализ
                List<Token> tokens = LexicalAnalyzer.analyze(code);
                StringBuilder result = new StringBuilder();
                for (Token token : tokens) {
                    result.append("Лексема: ").append(token.getValue())
                            .append(" || Тип: ").append(token.getType())
                            .append(" || Строка: ").append(token.getLine()).append("\n");
                }
                resultOutputArea.setText(result.toString());
                SaveToFile.writeToFile("output.txt", resultOutputArea.getText());

            } else if ("Синтаксический анализатор".equals(selectedMode)) {
                // Лексический анализ
                List<Token> tokens = LexicalAnalyzer.analyze(code);
                // Синтаксический анализ
                SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);
                syntaxAnalyzer.parse(); // Здесь будет генерироваться исключение, если код некорректен
                resultOutputArea.setText("Синтаксический анализ прошел успешно.");
                SaveToFile.writeToFile("output.txt", resultOutputArea.getText());
            } else if ("Генератор постфикса".equals(selectedMode)) {
                    // Лексический анализ
                    List<Token> tokens = LexicalAnalyzer.analyze(code);
                    // Генерация постфиксной записи
                    SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);
                    syntaxAnalyzer.parse();
                    String postfix = String.valueOf(PostfixGenerator.convertCodeToPostfix(code));
                    resultOutputArea.setText("Постфиксная запись:\n" + postfix);
                    // Сохранение результата в файл
                    SaveToFile.writeToFile("output.txt", postfix);


            }
            else if ("Генерация кода".equals(selectedMode)) {
                List<Token> tokens = LexicalAnalyzer.analyze(code);
                // Генерация постфиксной записи
                SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(tokens);
                syntaxAnalyzer.parse();
                String postfix = String.valueOf(PostfixGenerator.convertCodeToPostfix(code));
                CodeGenerator codeGenerator = new CodeGenerator();
                String cod = String.valueOf(codeGenerator.generateMnemonics(postfix));
                SaveToFile.writeToFile("cod.cod", cod);
                resultOutputArea.setText(cod);
            }


        } catch (LexicalException e) {
            resultOutputArea.setText("Ошибка лексического анализа: " + e.getMessage());
            SaveToFile.writeToFile("output.txt", resultOutputArea.getText());
        } catch (SyntaxException e) {
            resultOutputArea.setText("Ошибка синтаксического анализа: " + e.getMessage());
            SaveToFile.writeToFile("output.txt", resultOutputArea.getText());
        }
    }
}






