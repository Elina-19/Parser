package ru.itis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itis.model.Item;
import ru.itis.repository.ItemRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public void saveAll(List<Item> items) {
        itemRepository.saveAll(items);
    }
}
