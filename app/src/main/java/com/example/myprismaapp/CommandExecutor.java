package com.example.myprismaapp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class CommandExecutor {

    /**
     * Метод для выполнения команды в командной строке.
     *
     * @param command Строка команды, которую нужно выполнить.
     */
    public void executeCommand(String command) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Устанавливаем команду
        processBuilder.command("bash", "-c", command);

        try {
            // Запускаем процесс
            Process process = processBuilder.start();

            // Читаем стандартный вывод
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Читаем ошибки, если есть
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            // Ожидаем завершения процесса
            int exitCode = process.waitFor();
            System.out.println("Команда завершена с кодом: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}