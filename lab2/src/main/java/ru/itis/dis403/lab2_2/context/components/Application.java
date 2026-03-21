package ru.itis.dis403.lab2_2.context.components;

import org.springframework.stereotype.Component;
import ru.itis.dis403.lab2_2.context.model.Category;
import ru.itis.dis403.lab2_2.context.model.Order;
import ru.itis.dis403.lab2_2.context.model.Product;
import ru.itis.dis403.lab2_2.context.model.ImportProduct;

import java.math.BigDecimal;

@Component("App")
public class Application {

    private final MarketService marketService;

    public Application(MarketService marketService) {
        this.marketService = marketService;
    }

    public void run() {
        try {
            System.out.println("=== Market Application Started ===\n");

            // Создаем продукты
            Product computer = new Product("Компьютер", "0001", Category.PC, BigDecimal.valueOf(50000));
            Product laptop = new Product("Ноутбук", "0002", Category.LAPTOP, BigDecimal.valueOf(70000));
            Product phone = new Product("Телефон", "0003", Category.PHONE, BigDecimal.valueOf(30000));

            // Импортируем товары
            System.out.println("Importing products...");
            marketService.doImport(new ImportProduct(computer, 10, "Supplier A"));
            marketService.doImport(new ImportProduct(laptop, 5, "Supplier B"));
            marketService.doImport(new ImportProduct(phone, 20, "Supplier C"));

            // Делаем заказы
            System.out.println("\nPlacing orders...");
            marketService.doOrder(new Order(computer, 2, "Клиент 1"));
            marketService.doOrder(new Order(laptop, 1, "Клиент 2"));
            marketService.doOrder(new Order(phone, 5, "Клиент 3"));

            // Показываем остатки
            System.out.println("\nCurrent stock:");
            marketService.printProducts();

            // Показываем содержимое БД
            marketService.printDatabaseContents();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}