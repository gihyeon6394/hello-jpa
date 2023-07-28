package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class ItemService {

    @Autowired
    private ItemRepository repository;

    public void saveItem(Item item) {
        repository.save(item);
    }

    public List<Item> findItems() {
        return repository.findAll();
    }

    public Item findOne(Long id) {
        return repository.findOne(id);
    }

}
