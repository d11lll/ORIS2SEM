package ru.itis.dis403.lab2_2.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.itis.dis403.lab2_2.context.components.Application;
import ru.itis.dis403.lab2_2.context.config.Config;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext(Config.class);

        System.out.println("=== Spring Context Initialized ===");
        System.out.println("Beans in context:");
        for (String beanName : context.getBeanDefinitionNames()) {
            System.out.println("  - " + beanName);
        }

        System.out.println("\n" + "=".repeat(50) + "\n");

        // Запускаем приложение
        Application app = (Application) context.getBean("App");
        app.run();

        // Закрываем контекст при завершении
        ((AnnotationConfigApplicationContext) context).close();
    }
}