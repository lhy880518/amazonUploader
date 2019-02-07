package com.zeliter.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

    private Properties properties;

    public PropertiesReader(){
        properties = new Properties();
    }

    public Properties getProperties(){
        return properties;
    }

    public void loadProp(String path) throws IOException {
        InputStream inputStream = getClass().getResourceAsStream(path);
        properties.load(inputStream);
        inputStream.close();
    }

    public static Properties loadPropForStatic(String path) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = PropertiesReader.class.getClassLoader().getResourceAsStream(path);
        properties.load(inputStream);
        inputStream.close();
        return properties;
    }

}
