package ru.itis.dis403.lab2_4.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.itis.dis403.lab2_4.service.PersonService;

@Controller
@RequestMapping("/person")
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    // Страница с формой для ввода
    @GetMapping("/add")
    public String showForm() {
        return "person-add";
    }

    // Обработка отправки формы
    @PostMapping("/add")
    public String addPerson(@RequestParam String type,
                            @RequestParam String name,
                            @RequestParam String phone,
                            @RequestParam(required = false) String extra) {
        personService.savePerson(type, name, phone, extra);
        return "redirect:/person/list";
    }

    // Страница со списком всех лиц
    @GetMapping("/list")
    public String listPersons(Model model) {
        model.addAttribute("persons", personService.findAllPersons());
        return "person-list";
    }
}