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
            System.out.println("Запрашиваю зависимости: " + groupId + ":" + artifactId + ":" + version);
            
            String pomUrl = buildPomUrl(groupId, artifactId, version, repoUrl);
            System.out.println("URL POM: " + pomUrl);
            
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
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
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
            
            if (groupId != null && artifactId != null) {
                String dep = groupId + ":" + artifactId;
                if (version != null) {
                    dep += ":" + version;
                }
                dependencies.add(dep);
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