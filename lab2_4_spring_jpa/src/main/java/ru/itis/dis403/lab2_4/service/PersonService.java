package ru.itis.dis403.lab2_4.service;

import org.springframework.stereotype.Service;
import ru.itis.dis403.lab2_4.model.*;
import ru.itis.dis403.lab2_4.repository.PersonRepository;
import ru.itis.dis403.lab2_4.repository.PhoneRepository;

import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;
    private final PhoneRepository phoneRepository;

    public PersonService(PersonRepository personRepository, PhoneRepository phoneRepository) {
        this.personRepository = personRepository;
        this.phoneRepository = phoneRepository;
    }

    public void savePerson(String type, String name, String phoneNumber, String extra) {
        Phone phone = new Phone();
        phone.setNumber(phoneNumber);
        phoneRepository.save(phone);

        Person person;
        if ("admin".equals(type)) {
            Admin admin = new Admin();
            admin.setEmail(extra);
            person = admin;
        } else {
            Client client = new Client();
            client.setAddress(extra);
            person = client;
        }

        person.setName(name);
        person.setPhone(phone);
        personRepository.save(person);
    }

    public List<Person> findAllPersons() {
        return personRepository.findAll();
    }
}