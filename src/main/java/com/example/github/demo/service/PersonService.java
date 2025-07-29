package com.example.github.demo.service;

import com.example.github.demo.model.Person;
import com.example.github.demo.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public Optional<Person> getPersonById(Long id) {
        return personRepository.findById(id);
    }

    public Person createPerson(Person person) {
        return personRepository.save(person);
    }

    public Optional<Person> updatePerson(Long id, Person personDetails) {
        return personRepository.findById(id).map(existingPerson -> {
            existingPerson.setFirstName(personDetails.getFirstName());
            existingPerson.setLastName(personDetails.getLastName());
            existingPerson.setEmail(personDetails.getEmail());
            existingPerson.setPhoneNumber(personDetails.getPhoneNumber());
            existingPerson.setAddress(personDetails.getAddress());
            existingPerson.setAge(personDetails.getAge());
            return personRepository.save(existingPerson);
        });
    }

    public boolean deletePerson(Long id) {
        return personRepository.findById(id).map(person -> {
            personRepository.delete(person);
            return true;
        }).orElse(false);
    }
}
