// ru.itis.dis403.lab2_2.orm.DatabaseSchemaValidator.java
package ru.itis.dis403.lab2_2.orm;

import ru.itis.dis403.lab2_2.orm.annotation.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.*;

public class DatabaseSchemaValidator {

    private final Connection connection;

    public DatabaseSchemaValidator(Connection connection) {
        this.connection = connection;
    }

    public void createTables(List<Class<?>> entities) throws SQLException {
        // Сортируем таблицы в правильном порядке (без зависимостей -> с зависимостями)
        List<Class<?>> sortedEntities = topologicalSort(entities);

        for (Class<?> entity : sortedEntities) {
            createTable(entity);
        }
    }

    private List<Class<?>> topologicalSort(List<Class<?>> entities) {
        List<Class<?>> sorted = new ArrayList<>();
        Set<Class<?>> visited = new HashSet<>();
        Set<Class<?>> visiting = new HashSet<>();

        for (Class<?> entity : entities) {
            if (!visited.contains(entity)) {
                dfs(entity, entities, visited, visiting, sorted);
            }
        }

        return sorted;
    }

    private void dfs(Class<?> entity, List<Class<?>> allEntities,
                     Set<Class<?>> visited, Set<Class<?>> visiting,
                     List<Class<?>> sorted) {
        if (visiting.contains(entity)) {
            throw new RuntimeException("Circular dependency detected for " + entity.getSimpleName());
        }

        if (visited.contains(entity)) {
            return;
        }

        visiting.add(entity);

        // Находим все зависимости (таблицы, на которые ссылается эта сущность)
        for (Field field : entity.getDeclaredFields()) {
            if (field.isAnnotationPresent(ManyToOne.class)) {
                Class<?> dependency = field.getType();
                if (allEntities.contains(dependency)) {
                    dfs(dependency, allEntities, visited, visiting, sorted);
                }
            }
        }

        visiting.remove(entity);
        visited.add(entity);
        sorted.add(entity);
    }

    private void createTable(Class<?> entityClass) throws SQLException {
        String tableName = ModelScanner.getTableName(entityClass);

        if (tableExists(tableName)) {
            System.out.println("Table " + tableName + " already exists, validating...");
            validateTableStructure(entityClass, tableName);
            return;
        }

        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (\n");

        List<Field> fields = ModelScanner.getColumnFields(entityClass);
        List<String> columnDefinitions = new ArrayList<>();
        List<String> foreignKeys = new ArrayList<>();

        for (Field field : fields) {
            String columnName = ModelScanner.getColumnName(field);
            String columnType = getSqlType(field.getType());

            if (field.isAnnotationPresent(Id.class)) {
                columnDefinitions.add("    " + columnName + " " + columnType + " PRIMARY KEY");
            } else if (field.isAnnotationPresent(ManyToOne.class)) {
                columnDefinitions.add("    " + columnName + " bigint");
                String referencedTable = getReferencedTable(field);
                foreignKeys.add("    FOREIGN KEY (" + columnName +
                        ") REFERENCES " + referencedTable + "(id)");
            } else {
                columnDefinitions.add("    " + columnName + " " + columnType);
            }
        }

        sql.append(String.join(",\n", columnDefinitions));

        if (!foreignKeys.isEmpty()) {
            sql.append(",\n").append(String.join(",\n", foreignKeys));
        }

        sql.append("\n);");

        System.out.println("Creating table: " + tableName);
        System.out.println("SQL: " + sql);

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
            System.out.println("Table " + tableName + " created successfully");
        } catch (SQLException e) {
            System.err.println("Error creating table " + tableName + ": " + e.getMessage());
            throw e;
        }
    }

    private boolean tableExists(String tableName) throws SQLException {
        String sql = "SELECT table_name FROM information_schema.tables " +
                "WHERE table_type = 'BASE TABLE' AND table_schema NOT IN " +
                "('pg_catalog', 'information_schema')";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                if (rs.getString("table_name").equals(tableName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void validateTableStructure(Class<?> entityClass, String tableName)
            throws SQLException {
        Set<String> tableColumns = getTableColumns(tableName);
        List<Field> fields = ModelScanner.getColumnFields(entityClass);

        for (Field field : fields) {
            String columnName = ModelScanner.getColumnName(field);
            if (!tableColumns.contains(columnName)) {
                System.err.println("WARNING: Column " + columnName +
                        " not found in table " + tableName);
            }
        }
    }

    private Set<String> getTableColumns(String tableName) throws SQLException {
        Set<String> columns = new HashSet<>();

        String sql = "SELECT a.attname FROM pg_catalog.pg_attribute a " +
                "WHERE a.attrelid = ( " +
                "    SELECT c.oid FROM pg_catalog.pg_class c " +
                "    LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " +
                "    WHERE pg_catalog.pg_table_is_visible(c.oid) AND c.relname = ? " +
                ") AND a.attnum > 0 AND NOT a.attisdropped";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("attname"));
                }
            }
        }

        return columns;
    }

    private String getSqlType(Class<?> javaType) {
        if (javaType == Long.class || javaType == long.class) {
            return "bigserial";
        } else if (javaType == Integer.class || javaType == int.class) {
            return "integer";
        } else if (javaType == String.class) {
            return "varchar(255)";
        } else if (javaType == BigDecimal.class) {
            return "decimal(19,2)";
        } else if (javaType == LocalDateTime.class) {
            return "timestamp";
        } else {
            return "varchar(255)";
        }
    }

    private String getReferencedTable(Field field) {
        Class<?> referencedType = field.getType();
        return referencedType.getSimpleName().toLowerCase();
    }
}