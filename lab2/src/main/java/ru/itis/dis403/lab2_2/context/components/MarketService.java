package ru.itis.dis403.lab2_2.context.components;

import org.springframework.stereotype.Component;
import ru.itis.dis403.lab2_2.context.entity.*;
import ru.itis.dis403.lab2_2.context.model.*;
import ru.itis.dis403.lab2_2.orm.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class MarketService {

    private final Market market;
    private final EntityManagerFactory emf;

    public MarketService(Market market, EntityManagerFactory emf) {
        this.market = market;
        this.emf = emf;
    }

    public void doOrder(Order order) {
        Integer count = market.getProducts().get(order.getProduct());
        if(count == null || count < order.getCount()) {
            throw new NoSuchElementException("Not enough products in stock");
        }

        // Сохраняем заказ в БД через ORM
        try (EntityManager em = emf.createEntityManager()) {
            // Находим или создаем продукт в БД
            ProductEntity productEntity = findOrCreateProduct(order.getProduct(), em);

            OrderEntity orderEntity = new OrderEntity(
                    null,
                    order.getClient(),
                    LocalDateTime.now(),
                    order.getCount(),
                    "NEW",
                    productEntity
            );

            em.save(orderEntity);

            // Обновляем остатки
            market.getProducts().put(order.getProduct(), count - order.getCount());
            market.getOrders().add(order);

            System.out.println("Order saved with ID: " + orderEntity.getId());
        } catch (Exception e) {
            throw new RuntimeException("Error saving order", e);
        }
    }

    public void doImport(ImportProduct importProduct) {
        Integer count = market.getProducts().get(importProduct.getProduct());
        if (count == null) {
            count = 0;
        }

        // Сохраняем импорт в БД через ORM
        try (EntityManager em = emf.createEntityManager()) {
            ProductEntity productEntity = findOrCreateProduct(importProduct.getProduct(), em);

            // Обновляем количество
            productEntity.setQuantity(count + importProduct.getCount());
            em.save(productEntity);

            market.getProducts().put(importProduct.getProduct(), count + importProduct.getCount());
            market.getImportProducts().add(importProduct);
        } catch (Exception e) {
            throw new RuntimeException("Error saving import", e);
        }
    }

    private ProductEntity findOrCreateProduct(Product product, EntityManager em) {
        // Пытаемся найти продукт по артикулу
        List<ProductEntity> products = em.findAll(ProductEntity.class);
        for (ProductEntity pe : products) {
            if (pe.getArticul().equals(product.getArticul())) {
                return pe;
            }
        }

        // Создаем новый
        CategoryEntity categoryEntity = findOrCreateCategory(product.getCategory(), em);

        ProductEntity productEntity = new ProductEntity(
                null,
                product.getName(),
                product.getArticul(),
                product.getPrice(),
                0,
                categoryEntity
        );

        return em.save(productEntity);
    }

    private CategoryEntity findOrCreateCategory(Category category, EntityManager em) {
        List<CategoryEntity> categories = em.findAll(CategoryEntity.class);
        for (CategoryEntity ce : categories) {
            if (ce.getName().equals(category.name())) {
                return ce;
            }
        }

        CategoryEntity categoryEntity = new CategoryEntity(
                null,
                category.name(),
                "Category " + category.name()
        );

        return em.save(categoryEntity);
    }

    public void printProducts() {
        market.getProducts()
                .entrySet()
                .forEach(e -> System.out.println(e.getKey() + " : " + e.getValue()));
    }

    public void printDatabaseContents() {
        try (EntityManager em = emf.createEntityManager()) {
            System.out.println("\n=== Database Contents ===");

            List<CategoryEntity> categories = em.findAll(CategoryEntity.class);
            System.out.println("Categories:");
            categories.forEach(c -> System.out.println("  - " + c.getName()));

            List<ProductEntity> products = em.findAll(ProductEntity.class);
            System.out.println("Products:");
            products.forEach(p -> System.out.println("  - " + p.getName() +
                    " (" + p.getQuantity() + ")" + " - " + p.getCategory().getName()));

            List<OrderEntity> orders = em.findAll(OrderEntity.class);
            System.out.println("Orders:");
            orders.forEach(o -> System.out.println("  - " + o.getClientName() +
                    ": " + o.getQuantity() + " x " + o.getProduct().getName()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}