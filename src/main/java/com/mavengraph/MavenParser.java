package com.mavengraph;

import java.io.*;
import java.net.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.*;

public class MavenParser {
    
    public List<String> getDependencies(String groupId, String artifactId, String version, String repoUrl) {
        List<String> dependencies = new ArrayList<>();
        
        try {
            String pomUrl = buildPomUrl(groupId, artifactId, version, repoUrl);
            String pomContent = downloadPomFile(pomUrl);
            dependencies = parseDependenciesFromPom(pomContent);
            
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
        
        return dependencies;
    }
    
    private String buildPomUrl(String groupId, String artifactId, String version, String repoUrl) {
        String groupPath = groupId.replace('.', '/');
        
        String url = repoUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += groupPath + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
        
        return url;
    }
    
    private String downloadPomFile(String url) throws IOException {
        HttpURLConnection connection = null;
        try {
            URL pomUrl = new URL(url);
            connection = (HttpURLConnection) pomUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new IOException("HTTP ошибка: " + responseCode);
            }
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream())
            );
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
            reader.close();
            
            return content.toString();
            
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    private List<String> parseDependenciesFromPom(String pomContent) throws Exception {
        List<String> dependencies = new ArrayList<>();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(pomContent.getBytes()));
        
        NodeList dependencyNodes = doc.getElementsByTagName("dependency");
        
        for (int i = 0; i < dependencyNodes.getLength(); i++) {
            Element dependency = (Element) dependencyNodes.item(i);
            
            String groupId = getElementText(dependency, "groupId");
            String artifactId = getElementText(dependency, "artifactId");
            String version = getElementText(dependency, "version");
            String scope = getElementText(dependency, "scope");
            
            if ("test".equals(scope)) {
                continue;
            }
            if (version == null || version.contains("$")) {
                continue;
            }
            
            if (groupId != null && artifactId != null) {
                dependencies.add(groupId + ":" + artifactId + ":" + version);
            }
        }
        
        return dependencies;
    }
    
    private String getElementText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent().trim();
        }
        return null;
    }
}