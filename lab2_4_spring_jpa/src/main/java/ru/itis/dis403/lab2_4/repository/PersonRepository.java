package ru.itis.dis403.lab2_4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.dis403.lab2_4.model.Person;

public interface PersonRepository extends JpaRepository<Person, Long> {
}