package ru.itis.dis403.lab2_2.context.entity;

import ru.itis.dis403.lab2_2.orm.annotation.*;
import java.math.BigDecimal;

@Entity
public class ProductEntity {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String articul;

    @Column
    private BigDecimal price;

    @Column
    private Integer quantity;

    @ManyToOne
    private CategoryEntity category;

    public ProductEntity() {}

    public ProductEntity(Long id, String name, String articul,
                         BigDecimal price, Integer quantity, CategoryEntity category) {
        this.id = id;
        this.name = name;
        this.articul = articul;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArticul() { return articul; }
    public void setArticul(String articul) { this.articul = articul; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public CategoryEntity getCategory() { return category; }
    public void setCategory(CategoryEntity category) { this.category = category; }
}