package com.mavengraph;

import java.io.*;
import java.util.*;

public class DependencyGraph {
    private Map<String, DependencyNode> nodes;
    private List<String> cycles;
    private int maxDepth;

    public DependencyGraph() {
        this(3); // По умолчанию глубина 3
    }

    public DependencyGraph(int maxDepth) {
        nodes = new HashMap<>();
        cycles = new ArrayList<>();
        this.maxDepth = maxDepth;
    }

    public DependencyNode getOrCreateNode(String groupId, String artifactId, String version) {
        String id = groupId + ":" + artifactId + ":" + version;
        if (!nodes.containsKey(id)) {
            nodes.put(id, new DependencyNode(groupId, artifactId, version));
        }
        return nodes.get(id);
    }

    public void buildGraph(DependencyNode startNode, MavenParser parser, String repoUrl) {
        resetVisited();
        cycles.clear();
        buildGraphRecursive(startNode, parser, repoUrl, new HashSet<>(), new ArrayDeque<>(), 0);
    }

    private void buildGraphRecursive(DependencyNode node, MavenParser parser, String repoUrl,
                                     Set<String> visitedInPath, Deque<String> path, int depth) {
        if (depth >= maxDepth) {
            return;
        }

        String nodeId = node.getId();

        if (visitedInPath.contains(nodeId)) {
            cycles.add("Цикл: " + String.join(" -> ", path) + " -> " + nodeId);
            return;
        }

        if (node.isVisited()) {
            return;
        }

        node.setVisited(true);
        visitedInPath.add(nodeId);
        path.addLast(nodeId);

        List<String> directDeps = parser.getDependencies(
                node.getGroupId(), node.getArtifactId(), node.getVersion(), repoUrl
        );

        for (String dep : directDeps) {
            String[] parts = dep.split(":");
            if (parts.length >= 3) {
                String depGroupId = parts[0];
                String depArtifactId = parts[1];
                String depVersion = parts[2];

                // Пропускаем переменные версии
                if (depVersion.contains("$")) {
                    continue;
                }

                DependencyNode depNode = getOrCreateNode(depGroupId, depArtifactId, depVersion);
                node.addDependency(depNode);

                buildGraphRecursive(depNode, parser, repoUrl, visitedInPath, path, depth + 1);
            }
        }

        visitedInPath.remove(nodeId);
        path.removeLast();
    }

    public void buildFromTestFile(String filePath) throws IOException {
        nodes.clear();
        cycles.clear();

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            String[] parts = line.split("->");
            if (parts.length != 2) {
                continue;
            }

            String parent = parts[0].trim();
            String childrenStr = parts[1].trim();

            DependencyNode parentNode = getOrCreateNode(parent, parent, "1.0");

            if (!childrenStr.isEmpty()) {
                String[] children = childrenStr.split(",");
                for (String child : children) {
                    child = child.trim();
                    if (!child.isEmpty()) {
                        DependencyNode childNode = getOrCreateNode(child, child, "1.0");
                        parentNode.addDependency(childNode);
                    }
                }
            }
        }
        reader.close();

        detectCycles();
    }

    private void detectCycles() {
        cycles.clear();
        resetVisited();

        for (DependencyNode node : nodes.values()) {
            if (!node.isVisited()) {
                detectCyclesDFS(node, new HashSet<>(), new ArrayDeque<>());
            }
        }
    }

    private void detectCyclesDFS(DependencyNode node, Set<String> visitedInPath, Deque<String> path) {
        String nodeId = node.getId();

        if (visitedInPath.contains(nodeId)) {
            cycles.add("Цикл: " + String.join(" -> ", path) + " -> " + nodeId);
            return;
        }

        if (node.isVisited()) {
            return;
        }

        node.setVisited(true);
        visitedInPath.add(nodeId);
        path.addLast(nodeId);

        for (DependencyNode dep : node.getDependencies()) {
            detectCyclesDFS(dep, visitedInPath, path);
        }

        visitedInPath.remove(nodeId);
        path.removeLast();
    }

    private void resetVisited() {
        for (DependencyNode node : nodes.values()) {
            node.setVisited(false);
        }
    }

    public List<DependencyNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<String> getCycles() {
        return cycles;
    }

    public void printGraph() {
        System.out.println("\n=== Граф зависимостей (глубина: " + maxDepth + ") ===");
        System.out.println("Всего узлов: " + nodes.size());

        for (DependencyNode node : nodes.values()) {
            System.out.print(node.getId() + " -> ");
            if (node.getDependencies().isEmpty()) {
                System.out.println("(нет зависимостей)");
            } else {
                List<String> depIds = new ArrayList<>();
                for (DependencyNode dep : node.getDependencies()) {
                    depIds.add(dep.getId());
                }
                System.out.println(String.join(", ", depIds));
            }
        }

        if (!cycles.isEmpty()) {
            System.out.println("\n=== Обнаружены циклические зависимости ===");
            for (String cycle : cycles) {
                System.out.println(cycle);
            }
        }
    }
}