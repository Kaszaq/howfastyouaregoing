package pl.kaszaq;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import java.util.Properties;

public class Config {

    private static Config instance = null;
    private final Properties properties;

    public Config(Properties properties) {
        this.properties = properties;
    }

    public static synchronized Config getInstance() {
        if (instance == null) {
            Properties props = new Properties();
            try {

                props.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));
            } catch (IOException ex) {
                throw new RuntimeException("Failed to configuration", ex);
            }
            Config config = new Config(props);
            instance = config;
        }
        return instance;
    }

    public static final int HOUR_IN_MILLIS = 1000 * 60 * 60;

    public volatile static boolean cacheOnly = false;
    public static boolean refreshUnclosed = false;
    public static long refreshTime = HOUR_IN_MILLIS;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());

        OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
    }

    public String getJiraJsessionId() {
        return properties.getProperty("jira.auth.jsessionid");
    }

    public String getJiraUrl() {

        return properties.getProperty("jira.server.url");
    }
}
