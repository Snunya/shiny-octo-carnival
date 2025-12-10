package com.mavengraph;

import java.util.*;

public class DependencyNode {
    private String id; // groupId:artifactId:version
    private String groupId;
    private String artifactId;
    private String version;
    private List<DependencyNode> dependencies;
    private boolean visited;
    
    public DependencyNode(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.id = groupId + ":" + artifactId + ":" + version;
        this.dependencies = new ArrayList<>();
        this.visited = false;
    }
    
    public String getId() { return id; }
    public String getGroupId() { return groupId; }
    public String getArtifactId() { return artifactId; }
    public String getVersion() { return version; }
    public List<DependencyNode> getDependencies() { return dependencies; }
    public boolean isVisited() { return visited; }
    public void setVisited(boolean visited) { this.visited = visited; }
    
    public void addDependency(DependencyNode node) {
        dependencies.add(node);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DependencyNode that = (DependencyNode) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return id;
    }
}