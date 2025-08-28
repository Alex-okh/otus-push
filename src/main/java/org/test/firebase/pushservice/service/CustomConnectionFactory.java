package org.test.firebase.pushservice.service;

import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory class for creating database connections.
 *
 * <p>Provides a centralized way to obtain database connections for logger.</p>
 *
 * @author [Okhrimenko Alexey]
 */
@Slf4j
public class CustomConnectionFactory {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream input = CustomConnectionFactory.class.getClassLoader()
                                                              .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties not found!");
            }
            props.load(input);
            URL = props.getProperty("app.logging.dburl");
            USER = props.getProperty("app.logging.dbusr");
            PASSWORD = props.getProperty("app.logging.dbpwd");
        } catch (IOException ex) {
            log.error("Could not load Logging DB config", ex);
        }
    }

    /**
     * Creates and returns a database connection using predefined credentials.
     *
     * @return A new Connection object to the database
     * @throws SQLException If a database access error occurs or connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}