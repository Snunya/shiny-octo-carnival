\# Maven Dependency Graph Visualizer



Инструмент для визуализации графа зависимостей Maven-пакетов.



\## Сборка и запуск

```bash

javac -d target/classes src/main/java/com/mavengraph/\*.java

java -cp target/classes com.mavengraph.Main --help

Этапы разработки
Этап 1: CLI приложение с парсингом аргументов

Этап 2: Парсинг зависимостей из Maven Central

Этап 3: Построение графа зависимостей (DFS)

Этап 4: Дополнительные операции с графом

Этап 5: Визуализация в Mermaid/PNG

**2. .gitignore:**
```cmd
notepad .gitignore