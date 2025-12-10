package com.mavengraph;

import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) {
        Config config = parseArguments(args);
        
        System.out.println("=== Параметры конфигурации ===");
        for (Map.Entry<String, String> entry : config.getAllParams().entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        
        if (config.isTestMode()) {
            runTestMode(config);
        } else {
            runRealMode(config);
        }
        
        System.out.println("\nВыполнение завершено");
    }
    
    private static void runTestMode(Config config) {
        System.out.println("\n=== Тестовый режим ===");
        
        try {
            DependencyGraph graph = new DependencyGraph();
            graph.buildFromTestFile(config.getTestRepoPath());
            graph.printGraph();
            
            System.out.println("\nВсего узлов: " + graph.getAllNodes().size());
            if (!graph.getCycles().isEmpty()) {
                System.out.println("Обнаружено циклов: " + graph.getCycles().size());
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
        }
    }
    
    private static void runRealMode(Config config) {
        String[] parts = config.getPackageName().split(":");
        if (parts.length != 2) {
            System.err.println("Ошибка: неверный формат package");
            System.exit(1);
        }
        
        String groupId = parts[0];
        String artifactId = parts[1];
        
        System.out.println("\n=== Этап 2: Прямые зависимости ===");
        MavenParser parser = new MavenParser();
        List<String> dependencies = parser.getDependencies(
            groupId, artifactId, config.getVersion(), config.getRepoUrl()
        );
        
        if (dependencies.isEmpty()) {
            System.out.println("Зависимостей не найдено");
        } else {
            for (int i = 0; i < dependencies.size(); i++) {
                System.out.println((i + 1) + ". " + dependencies.get(i));
            }
            System.out.println("Всего: " + dependencies.size() + " зависимостей");
        }
        
        System.out.println("\n=== Этап 3: Граф зависимостей ===");
        DependencyGraph graph = new DependencyGraph(2); // Глубина 2 для теста
        DependencyNode rootNode = graph.getOrCreateNode(groupId, artifactId, config.getVersion());
        
        graph.buildGraph(rootNode, parser, config.getRepoUrl());
        graph.printGraph();
    }
    
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
                        System.err.println("Неизвестный параметр: " + args[i]);
                        printHelp();
                        System.exit(1);
                }
            }
            
            validateConfig(config);
            
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            printHelp();
            System.exit(1);
        }
        
        return config;
    }
    
    private static void checkArgumentExists(String[] args, int index, String argName) {
        if (index + 1 >= args.length) {
            System.err.println("Ошибка: нет значения для " + argName);
            System.exit(1);
        }
    }
    
    private static void validateConfig(Config config) {
        if (!config.isTestMode()) {
            if (config.getPackageName() == null) {
                System.err.println("Ошибка: укажите --package");
                System.exit(1);
            }
            if (config.getVersion() == null) {
                System.err.println("Ошибка: укажите --version");
                System.exit(1);
            }
            if (!config.getPackageName().contains(":")) {
                System.err.println("Ошибка: --package должен быть groupId:artifactId");
                System.exit(1);
            }
        } else {
            if (config.getTestRepoPath() == null) {
                System.err.println("Ошибка: укажите --test-repo-path");
                System.exit(1);
            }
        }
    }
    
    private static void printHelp() {
        System.out.println("Использование:");
        System.out.println("  java -jar maven-dep-graph.jar [ОПЦИИ]");
        System.out.println("\nОпции:");
        System.out.println("  --package <groupId:artifactId>   Maven пакет");
        System.out.println("  --version <version>              Версия");
        System.out.println("  --repo-url <url>                 URL репозитория");
        System.out.println("  --test-mode                      Тестовый режим");
        System.out.println("  --test-repo-path <путь>          Путь к тестовому файлу");
        System.out.println("  --output <имя>                   Имя файла графа");
        System.out.println("  --help                           Справка");
        System.out.println("\nПримеры:");
        System.out.println("  java -jar maven-dep-graph.jar --package com.google.code.gson:gson --version 2.10.1");
        System.out.println("  java -jar maven-dep-graph.jar --test-mode --test-repo-path test.txt");
    }
}