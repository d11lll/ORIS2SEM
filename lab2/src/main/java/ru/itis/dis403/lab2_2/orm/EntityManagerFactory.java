// ru.itis.dis403.lab2_2.orm.EntityManagerFactory.java
package ru.itis.dis403.lab2_2.orm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EntityManagerFactory implements AutoCloseable {

    private HikariDataSource dataSource;
    private String ddlOption;
    private List<Class<?>> entities;

    public EntityManagerFactory(String configFile) throws Exception {
        loadConfiguration(configFile);
        scanEntities();
        initializeDatabase();
    }

    private void loadConfiguration(String configFile) throws Exception {
        Yaml yaml = new Yaml();

        // Используем ClassPathResource от Spring
        ClassPathResource resource = new ClassPathResource(configFile);

        if (!resource.exists()) {
            throw new RuntimeException("Configuration file not found in classpath: " + configFile);
        }

        // Читаем конфигурацию
        Map<String, Object> data;
        try (InputStream is = resource.getInputStream()) {
            data = yaml.load(is);
        }

        Map<String, Object> dbConfig = (Map<String, Object>) data.get("dbconfig");

        String driver = (String) dbConfig.get("jdbcdriver");
        String url = (String) dbConfig.get("jdbcurl");
        String user = (String) dbConfig.get("dbuser");
        String password = (String) dbConfig.get("dbpassword");
        int poolSize = (int) dbConfig.get("poolsize");
        ddlOption = (String) dbConfig.get("ddloption");

        System.out.println("Database config loaded from classpath:");
        System.out.println("  URL: " + url);
        System.out.println("  User: " + user);
        System.out.println("  Pool size: " + poolSize);
        System.out.println("  DDL option: " + ddlOption);

        Class.forName(driver);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setMaximumPoolSize(poolSize);
        config.setConnectionTimeout(50000);

        dataSource = new HikariDataSource(config);
    }

    private void scanEntities() throws Exception {
        entities = ModelScanner.findEntities("ru.itis.dis403.lab2_2.context.entity");
        System.out.println("Found " + entities.size() + " entities:");
        entities.forEach(e -> System.out.println("  - " + e.getSimpleName()));
    }

    private void initializeDatabase() throws SQLException {
        if ("create".equalsIgnoreCase(ddlOption)) {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseSchemaValidator validator = new DatabaseSchemaValidator(conn);
                validator.createTables(entities);
            }
        }
    }

    public EntityManager createEntityManager() {
        try {
            Connection connection = dataSource.getConnection();
            return new EntityManagerImpl(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error creating EntityManager", e);
        }
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}