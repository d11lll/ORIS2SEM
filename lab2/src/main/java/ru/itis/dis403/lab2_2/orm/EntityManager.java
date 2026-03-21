package ru.itis.dis403.lab2_2.orm;

import java.util.List;

public interface EntityManager extends AutoCloseable {
    <T> T save(T entity);
    void remove(Object entity);
    <T> T find(Class<T> entityType, Object key);
    <T> List<T> findAll(Class<T> entityType);
}