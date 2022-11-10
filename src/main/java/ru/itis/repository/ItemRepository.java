package ru.itis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itis.model.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
