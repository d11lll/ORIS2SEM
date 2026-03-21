package ru.itis.dis403.lab2_2.context.entity;

import ru.itis.dis403.lab2_2.orm.annotation.*;

@Entity
public class CategoryEntity {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String description;

    public CategoryEntity() {}

    public CategoryEntity(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}