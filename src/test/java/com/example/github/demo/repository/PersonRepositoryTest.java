package com.example.github.demo.repository;

import com.example.github.demo.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PersonRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PersonRepository personRepository;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("123-456-7890");
        testPerson.setAddress("123 Main St");
        testPerson.setAge(30);
    }

    @Test
    @DisplayName("Test save and find person")
    void testSaveAndFindPerson() {
        // Save person
        Person savedPerson = personRepository.save(testPerson);
        
        assertNotNull(savedPerson.getId());
        assertEquals("John", savedPerson.getFirstName());
        
        // Find by ID
        Optional<Person> foundPerson = personRepository.findById(savedPerson.getId());
        assertTrue(foundPerson.isPresent());
        assertEquals("john.doe@example.com", foundPerson.get().getEmail());
    }

    @Test
    @DisplayName("Test find all persons")
    void testFindAllPersons() {
        // Save multiple persons
        Person person1 = new Person(null, "John", "Doe", "john@example.com", "123", "addr1", 30);
        Person person2 = new Person(null, "Jane", "Smith", "jane@example.com", "456", "addr2", 25);
        
        personRepository.save(person1);
        personRepository.save(person2);
        
        List<Person> persons = personRepository.findAll();
        assertTrue(persons.size() >= 2);
    }

    @Test
    @DisplayName("Test find by non-existent ID")
    void testFindByNonExistentId() {
        Optional<Person> person = personRepository.findById(999L);
        assertFalse(person.isPresent());
    }

    @Test
    @DisplayName("Test delete person")
    void testDeletePerson() {
        // Save person
        Person savedPerson = personRepository.save(testPerson);
        Long personId = savedPerson.getId();
        
        // Verify it exists
        assertTrue(personRepository.findById(personId).isPresent());
        
        // Delete person
        personRepository.delete(savedPerson);
        
        // Verify it's deleted
        assertFalse(personRepository.findById(personId).isPresent());
    }

    @Test
    @DisplayName("Test update person")
    void testUpdatePerson() {
        // Save person
        Person savedPerson = personRepository.save(testPerson);
        
        // Update person
        savedPerson.setFirstName("Updated");
        savedPerson.setAge(35);
        Person updatedPerson = personRepository.save(savedPerson);
        
        assertEquals("Updated", updatedPerson.getFirstName());
        assertEquals(35, updatedPerson.getAge());
        assertEquals(savedPerson.getId(), updatedPerson.getId());
    }

    @Test
    @DisplayName("Test exists by ID")
    void testExistsById() {
        Person savedPerson = personRepository.save(testPerson);
        
        assertTrue(personRepository.existsById(savedPerson.getId()));
        assertFalse(personRepository.existsById(999L));
    }

    @Test
    @DisplayName("Test count persons")
    void testCountPersons() {
        long initialCount = personRepository.count();
        
        personRepository.save(testPerson);
        
        assertEquals(initialCount + 1, personRepository.count());
    }
}
