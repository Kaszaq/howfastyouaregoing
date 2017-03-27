package pl.kaszaq.agile;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author michal.kasza
 */
public class AgileClientFactory {

    private static AgileClientFactory INSTANCE = new AgileClientFactory();

    private AgileClientFactory() {
    }

    public static AgileClientFactory getInstance() {
        return INSTANCE;
    }
//    

    public AgileClientConstructor newClient() {
        return new AgileClientConstructor();
    }

    public static class AgileClientConstructor {

        Map<String, AgileProjectConfiguration> configuration = new HashMap<>();

        private AgileClientConstructor() {
        }

        public AgileClientConstructor withAgileProjectConfig(String projectId, AgileProjectConfiguration projectConfiguration) {
            configuration.put(projectId, projectConfiguration);
            return this;
        }

        public AgileClient create() {
            return new AgileClient(configuration);
        }
    }

}
