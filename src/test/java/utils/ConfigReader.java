package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads values from src/test/resources/config.properties.
 *
 * Usage from any test class:
 *   String accessKey = ConfigReader.get("dai.accessKey");
 *   String axeKey    = ConfigReader.get("axe.apiKey");
 */
public final class ConfigReader {

    private static final String CONFIG_FILE = "config.properties";
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        CONFIG_FILE + " not found on the classpath (expected in src/test/resources)");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Failed to load " + CONFIG_FILE + ": " + e);
        }
    }

    private ConfigReader() {}

    public static String get(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing or empty config key '" + key + "' in " + CONFIG_FILE);
        }
        return value.trim();
    }
}
