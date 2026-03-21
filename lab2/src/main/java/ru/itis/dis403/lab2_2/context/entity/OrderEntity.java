package ru.itis.dis403.lab2_2.context.entity;

import ru.itis.dis403.lab2_2.orm.annotation.*;
import java.time.LocalDateTime;

@Entity
public class OrderEntity {

    @Id
    private Long id;

    @Column
    private String clientName;

    @Column
    private LocalDateTime orderDate;

    @Column
    private Integer quantity;

    @Column
    private String status;

    @ManyToOne
    private ProductEntity product;

    public OrderEntity() {}

    public OrderEntity(Long id, String clientName, LocalDateTime orderDate,
                       Integer quantity, String status, ProductEntity product) {
        this.id = id;
        this.clientName = clientName;
        this.orderDate = orderDate;
        this.quantity = quantity;
        this.status = status;
        this.product = product;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ProductEntity getProduct() { return product; }
    public void setProduct(ProductEntity product) { this.product = product; }
}