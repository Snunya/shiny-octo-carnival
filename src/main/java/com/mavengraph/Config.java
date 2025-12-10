package com.mavengraph;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для хранения конфигурации приложения
 * Этап 1: Хранение параметров командной строки
 */
public class Config {
    private String packageName;
    private String version;
    private String repoUrl = "https://repo1.maven.org/maven2/";
    private boolean testMode = false;
    private String testRepoPath;
    private String outputFile = "graph.png";
    
    // Геттеры
    public String getPackageName() { return packageName; }
    public String getVersion() { return version; }
    public String getRepoUrl() { return repoUrl; }
    public boolean isTestMode() { return testMode; }
    public String getTestRepoPath() { return testRepoPath; }
    public String getOutputFile() { return outputFile; }
    
    // Сеттеры
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setVersion(String version) { this.version = version; }
    public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }
    public void setTestMode(boolean testMode) { this.testMode = testMode; }
    public void setTestRepoPath(String testRepoPath) { this.testRepoPath = testRepoPath; }
    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }
    
    /**
     * Получить все параметры в формате ключ-значение
     * Требование Этапа 1: вывод всех параметров
     */
    public Map<String, String> getAllParams() {
        Map<String, String> params = new HashMap<>();
        params.put("package", packageName != null ? packageName : "не указан");
        params.put("version", version != null ? version : "не указана");
        params.put("repoUrl", repoUrl);
        params.put("testMode", Boolean.toString(testMode));
        params.put("testRepoPath", testRepoPath != null ? testRepoPath : "не указан");
        params.put("outputFile", outputFile);
        return params;
    }
}