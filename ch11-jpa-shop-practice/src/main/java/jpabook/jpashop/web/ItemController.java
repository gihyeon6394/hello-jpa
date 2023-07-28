package jpabook.jpashop.web;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class ItemController {

    @Autowired
    private ItemService itemService;

    @RequestMapping(value = "/items/new", method = RequestMethod.GET)
    public String createFrm() {
        return "items/createItemForm";
    }

    @RequestMapping(value = "/items/new", method = RequestMethod.POST)
    public String create(Book item) {
        itemService.saveItem(item);
        return "redirect:/items";
    }

    @RequestMapping(value = "items/{itemId}/edit", method = RequestMethod.GET)
    public String updateItemFrm(@PathVariable("itemId") Long itemId, Model model) {
        Book item = (Book) itemService.findOne(itemId);
        model.addAttribute("item", item);
        return "items/updateItemForm";
    }

    @RequestMapping(value = "items/{itemId}/edit", method = RequestMethod.POST)
    public String updateItem(@PathVariable("item") Book item) {
        itemService.saveItem(item);
        return "redirect:/items";
    }
}
