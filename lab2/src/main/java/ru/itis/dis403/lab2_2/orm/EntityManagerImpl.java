// ru.itis.dis403.lab2_2.orm.EntityManagerImpl.java
package ru.itis.dis403.lab2_2.orm;

import ru.itis.dis403.lab2_2.orm.annotation.*;
import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class EntityManagerImpl implements EntityManager, AutoCloseable {

    private final Connection connection;

    public EntityManagerImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public <T> T save(T entity) {
        try {
            Class<?> entityClass = entity.getClass();
            String tableName = ModelScanner.getTableName(entityClass);
            Field idField = ModelScanner.getIdField(entityClass);
            idField.setAccessible(true);

            Object idValue = idField.get(entity);

            if (idValue == null) {
                return insert(entity, tableName, idField);
            } else {
                return update(entity, tableName, idField);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving entity", e);
        }
    }

    private <T> T insert(T entity, String tableName, Field idField) throws Exception {
        List<Field> fields = ModelScanner.getColumnFields(entity.getClass());

        List<String> columnNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) continue;

            field.setAccessible(true);
            String columnName = ModelScanner.getColumnName(field);

            Object value = field.get(entity);
            if (field.isAnnotationPresent(ManyToOne.class) && value != null) {
                Field referencedIdField = ModelScanner.getIdField(value.getClass());
                referencedIdField.setAccessible(true);
                value = referencedIdField.get(value);
            }

            if (value != null) {
                columnNames.add(columnName);
                placeholders.add("?");
                values.add(value);
            }
        }

        String sql = "INSERT INTO " + tableName + " (" +
                String.join(", ", columnNames) + ") VALUES (" +
                String.join(", ", placeholders) + ") RETURNING id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                // Special handling for LocalDateTime
                if (value instanceof LocalDateTime) {
                    pstmt.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) value));
                } else {
                    pstmt.setObject(i + 1, value);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                idField.setAccessible(true);
                idField.set(entity, rs.getLong(1));
            }
        }

        return entity;
    }

    private <T> T update(T entity, String tableName, Field idField) throws Exception {
        List<Field> fields = ModelScanner.getColumnFields(entity.getClass());

        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Object idValue = idField.get(entity);

        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) continue;

            field.setAccessible(true);
            String columnName = ModelScanner.getColumnName(field);

            Object value = field.get(entity);
            if (field.isAnnotationPresent(ManyToOne.class) && value != null) {
                Field referencedIdField = ModelScanner.getIdField(value.getClass());
                referencedIdField.setAccessible(true);
                value = referencedIdField.get(value);
            }

            setClauses.add(columnName + " = ?");
            values.add(value);
        }

        String sql = "UPDATE " + tableName + " SET " +
                String.join(", ", setClauses) + " WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.size(); i++) {
                Object value = values.get(i);
                if (value instanceof LocalDateTime) {
                    pstmt.setTimestamp(i + 1, Timestamp.valueOf((LocalDateTime) value));
                } else {
                    pstmt.setObject(i + 1, value);
                }
            }
            pstmt.setObject(values.size() + 1, idValue);
            pstmt.executeUpdate();
        }

        return entity;
    }

    @Override
    public void remove(Object entity) {
        try {
            Class<?> entityClass = entity.getClass();
            String tableName = ModelScanner.getTableName(entityClass);
            Field idField = ModelScanner.getIdField(entityClass);
            idField.setAccessible(true);
            Object idValue = idField.get(entity);

            String sql = "DELETE FROM " + tableName + " WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setObject(1, idValue);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error removing entity", e);
        }
    }

    @Override
    public <T> T find(Class<T> entityType, Object key) {
        try {
            String tableName = ModelScanner.getTableName(entityType);

            String sql = "SELECT * FROM " + tableName + " WHERE id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setObject(1, key);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    return mapResultSetToEntity(entityType, rs);
                }
            }

            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error finding entity", e);
        }
    }

    @Override
    public <T> List<T> findAll(Class<T> entityType) {
        List<T> results = new ArrayList<>();

        try {
            String tableName = ModelScanner.getTableName(entityType);
            String sql = "SELECT * FROM " + tableName;

            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    results.add(mapResultSetToEntity(entityType, rs));
                }
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("Error finding all entities", e);
        }
    }

    private <T> T mapResultSetToEntity(Class<T> entityType, ResultSet rs) throws Exception {
        T entity = entityType.getDeclaredConstructor().newInstance();

        List<Field> fields = ModelScanner.getColumnFields(entityType);
        for (Field field : fields) {
            field.setAccessible(true);
            String columnName = ModelScanner.getColumnName(field);

            if (field.isAnnotationPresent(ManyToOne.class)) {
                Long referencedId = rs.getObject(columnName, Long.class);
                if (referencedId != null) {
                    Object referencedEntity = find(field.getType(), referencedId);
                    field.set(entity, referencedEntity);
                }
            } else {
                Object value = rs.getObject(columnName);
                if (value != null) {
                    // Special handling for LocalDateTime
                    if (field.getType() == LocalDateTime.class && value instanceof Timestamp) {
                        value = ((Timestamp) value).toLocalDateTime();
                    }
                    field.set(entity, value);
                }
            }
        }

        return entity;
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}