package com.mavengraph;

import java.util.Map;

/**
 * Главный класс приложения
 * Этап 1: CLI приложение с парсингом аргументов командной строки
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Maven Dependency Graph Visualizer ===");
        System.out.println("Этап 1: CLI приложение с конфигурацией\n");
        
        // Парсим аргументы
        Config config = parseArguments(args);
        
        // ВЫВОД ВСЕХ ПАРАМЕТРОВ (требование Этапа 1, пункт 3)
        System.out.println("=== Параметры конфигурации (ключ = значение) ===");
        for (Map.Entry<String, String> entry : config.getAllParams().entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        System.out.println("\n=== Информация о режиме ===");
        if (config.isTestMode()) {
            System.out.println("Режим: ТЕСТОВЫЙ");
            System.out.println("Используется файл: " + config.getTestRepoPath());
        } else {
            System.out.println("Режим: РЕАЛЬНЫЙ (Maven Central)");
            System.out.println("Анализируемый пакет: " + config.getPackageName());
            System.out.println("Версия: " + config.getVersion());
        }
        
        System.out.println("\nФайл для визуализации: " + config.getOutputFile());
        System.out.println("\n Этап 1 выполнен успешно!");
    }
    
    /**
     * Парсинг аргументов командной строки
     * Требование Этапа 1, пункт 1-2: источник параметров - опции командной строки
     */
    private static Config parseArguments(String[] args) {
        Config config = new Config();
        
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--package":
                        checkArgumentExists(args, i, "--package");
                        config.setPackageName(args[++i]);
                        break;
                        
                    case "--version":
                        checkArgumentExists(args, i, "--version");
                        config.setVersion(args[++i]);
                        break;
                        
                    case "--repo-url":
                        checkArgumentExists(args, i, "--repo-url");
                        config.setRepoUrl(args[++i]);
                        break;
                        
                    case "--test-mode":
                        config.setTestMode(true);
                        break;
                        
                    case "--test-repo-path":
                        checkArgumentExists(args, i, "--test-repo-path");
                        config.setTestRepoPath(args[++i]);
                        break;
                        
                    case "--output":
                        checkArgumentExists(args, i, "--output");
                        config.setOutputFile(args[++i]);
                        break;
                        
                    case "--help":
                        printHelp();
                        System.exit(0);
                        break;
                        
                    default:
                        System.err.println(" Ошибка: Неизвестный параметр: " + args[i]);
                        printHelp();
                        System.exit(1);
                }
            }
            
            // ВАЛИДАЦИЯ ПАРАМЕТРОВ (требование Этапа 1, пункт 4)
            validateConfig(config);
            
        } catch (Exception e) {
            System.err.println(" Ошибка при парсинге аргументов: " + e.getMessage());
            printHelp();
            System.exit(1);
        }
        
        return config;
    }
    
    /**
     * Проверка наличия значения для аргумента
     */
    private static void checkArgumentExists(String[] args, int index, String argName) {
        if (index + 1 >= args.length) {
            System.err.println(" Ошибка: для параметра " + argName + " не указано значение");
            System.exit(1);
        }
    }
    
    /**
     * Валидация конфигурации
     * Требование Этапа 1, пункт 4: обработка ошибок параметров
     */
    private static void validateConfig(Config config) {
        if (!config.isTestMode()) {
            // Режим Maven Central
            if (config.getPackageName() == null) {
                System.err.println(" Ошибка: в реальном режиме необходимо указать --package");
                System.exit(1);
            }
            if (config.getVersion() == null) {
                System.err.println(" Ошибка: в реальном режиме необходимо указать --version");
                System.exit(1);
            }
            // Проверка формата package (группа:артефакт)
            if (!config.getPackageName().contains(":")) {
                System.err.println(" Ошибка: --package должен быть в формате groupId:artifactId");
                System.exit(1);
            }
        } else {
            // Тестовый режим
            if (config.getTestRepoPath() == null) {
                System.err.println(" Ошибка: в тестовом режиме необходимо указать --test-repo-path");
                System.exit(1);
            }
        }
    }
    
    /**
     * Вывод справки
     */
    private static void printHelp() {
        System.out.println("\n Справка по использованию:");
        System.out.println("java -jar maven-dep-graph.jar [ОПЦИИ]");
        System.out.println("\nОПЦИИ:");
        System.out.println("  --package <groupId:artifactId>   Анализируемый Maven-пакет");
        System.out.println("  --version <version>              Версия пакета");
        System.out.println("  --repo-url <url>                 URL Maven-репозитория (по умолчанию: https://repo1.maven.org/maven2/)");
        System.out.println("  --test-mode                      Включить тестовый режим");
        System.out.println("  --test-repo-path <путь>          Путь к файлу тестового репозитория");
        System.out.println("  --output <имя_файла>             Имя файла для графа (по умолчанию: graph.png)");
        System.out.println("  --help                           Показать эту справку");
        System.out.println("\n ПРИМЕРЫ:");
        System.out.println("  Реальный режим (Maven Central):");
        System.out.println("    java -jar maven-dep-graph.jar --package org.springframework:spring-core --version 5.3.23");
        System.out.println("  Тестовый режим:");
        System.out.println("    java -jar maven-dep-graph.jar --test-mode --test-repo-path test-data.txt");
        System.out.println("\n Этап 1: Минимальный прототип с конфигурацией");
    }
}