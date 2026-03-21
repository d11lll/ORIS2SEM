package ru.itis.dis403.lab2_2.orm;

import ru.itis.dis403.lab2_2.orm.annotation.*;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class ModelScanner {

    public static List<Class<?>> findEntities(String packageName) throws Exception {
        List<Class<?>> entities = new ArrayList<>();
        String path = packageName.replace('.', '/');
        URL resource = Thread.currentThread().getContextClassLoader().getResource(path);

        if (resource == null) {
            throw new RuntimeException("Package not found: " + packageName);
        }

        File directory = new File(resource.getFile());
        scanDirectory(directory, packageName, entities);

        return entities;
    }

    private static void scanDirectory(File directory, String packageName,
                                      List<Class<?>> entities) throws ClassNotFoundException {
        if (!directory.exists()) return;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), entities);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." +
                        file.getName().substring(0, file.getName().length() - 6);
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(Entity.class)) {
                    entities.add(clazz);
                }
            }
        }
    }

    public static String getTableName(Class<?> entityClass) {
        return entityClass.getSimpleName().toLowerCase();
    }

    public static Field getIdField(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        return null;
    }

    public static List<Field> getColumnFields(Class<?> entityClass) {
        List<Field> fields = new ArrayList<>();
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) ||
                    field.isAnnotationPresent(Id.class) ||
                    field.isAnnotationPresent(ManyToOne.class)) {
                fields.add(field);
            }
        }
        return fields;
    }

    public static String getColumnName(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class)) {
            return field.getName() + "_id";
        }

        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.name().isEmpty()) {
                return column.name();
            }
        }

        return field.getName();
    }
}