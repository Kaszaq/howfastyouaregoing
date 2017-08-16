package pl.kaszaq.howfastyouaregoing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

public class Config {

    /**
     * Causes not to call external services and use only cached data.
     *
     * @deprecated this should not be used in most real live cases - mostly
     * useful for development. This flag may and probably will be
     * removed/replaced in future.
     */
    @Deprecated
    public volatile static boolean cacheOnly = false;
    public static boolean refreshUnclosed = false;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
