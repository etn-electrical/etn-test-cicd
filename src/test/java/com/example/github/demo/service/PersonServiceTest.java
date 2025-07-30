package com.example.github.demo.service;

import com.example.github.demo.model.Person;
import com.example.github.demo.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private Person testPerson;
    private Person updatedPerson;

    @BeforeEach
    void setUp() {
        testPerson = new Person();
        testPerson.setId(1L);
        testPerson.setFirstName("John");
        testPerson.setLastName("Doe");
        testPerson.setEmail("john.doe@example.com");
        testPerson.setPhoneNumber("123-456-7890");
        testPerson.setAddress("123 Main St");
        testPerson.setAge(30);

        updatedPerson = new Person();
        updatedPerson.setFirstName("Jane");
        updatedPerson.setLastName("Smith");
        updatedPerson.setEmail("jane.smith@example.com");
        updatedPerson.setPhoneNumber("098-765-4321");
        updatedPerson.setAddress("456 Elm St");
        updatedPerson.setAge(25);
    }

    @Test
    @DisplayName("Test get all persons")
    void testGetAllPersons() {
        // Arrange
        List<Person> expectedPersons = Arrays.asList(testPerson, updatedPerson);
        when(personRepository.findAll()).thenReturn(expectedPersons);

        // Act
        List<Person> actualPersons = personService.getAllPersons();

        // Assert
        assertEquals(2, actualPersons.size());
        assertEquals(expectedPersons, actualPersons);
        verify(personRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Test get person by ID - found")
    void testGetPersonByIdFound() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));

        // Act
        Optional<Person> result = personService.getPersonById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testPerson, result.get());
        verify(personRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Test get person by ID - not found")
    void testGetPersonByIdNotFound() {
        // Arrange
        when(personRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Person> result = personService.getPersonById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(personRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Test create person")
    void testCreatePerson() {
        // Arrange
        Person newPerson = new Person();
        newPerson.setFirstName("New");
        newPerson.setLastName("Person");
        
        Person savedPerson = new Person();
        savedPerson.setId(1L);
        savedPerson.setFirstName("New");
        savedPerson.setLastName("Person");
        
        when(personRepository.save(newPerson)).thenReturn(savedPerson);

        // Act
        Person result = personService.createPerson(newPerson);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New", result.getFirstName());
        verify(personRepository, times(1)).save(newPerson);
    }

    @Test
    @DisplayName("Test update person - found")
    void testUpdatePersonFound() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        
        Person updatedPersonWithId = new Person();
        updatedPersonWithId.setId(1L);
        updatedPersonWithId.setFirstName("Jane");
        updatedPersonWithId.setLastName("Smith");
        updatedPersonWithId.setEmail("jane.smith@example.com");
        updatedPersonWithId.setPhoneNumber("098-765-4321");
        updatedPersonWithId.setAddress("456 Elm St");
        updatedPersonWithId.setAge(25);
        
        when(personRepository.save(any(Person.class))).thenReturn(updatedPersonWithId);

        // Act
        Optional<Person> result = personService.updatePerson(1L, updatedPerson);

        // Assert
        assertTrue(result.isPresent());
        Person actualPerson = result.get();
        assertEquals("Jane", actualPerson.getFirstName());
        assertEquals("Smith", actualPerson.getLastName());
        assertEquals("jane.smith@example.com", actualPerson.getEmail());
        assertEquals("098-765-4321", actualPerson.getPhoneNumber());
        assertEquals("456 Elm St", actualPerson.getAddress());
        assertEquals(25, actualPerson.getAge());
        
        verify(personRepository, times(1)).findById(1L);
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    @DisplayName("Test update person - not found")
    void testUpdatePersonNotFound() {
        // Arrange
        when(personRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Person> result = personService.updatePerson(999L, updatedPerson);

        // Assert
        assertFalse(result.isPresent());
        verify(personRepository, times(1)).findById(999L);
        verify(personRepository, never()).save(any(Person.class));
    }

    @Test
    @DisplayName("Test delete person - found")
    void testDeletePersonFound() {
        // Arrange
        when(personRepository.findById(1L)).thenReturn(Optional.of(testPerson));
        doNothing().when(personRepository).delete(testPerson);

        // Act
        boolean result = personService.deletePerson(1L);

        // Assert
        assertTrue(result);
        verify(personRepository, times(1)).findById(1L);
        verify(personRepository, times(1)).delete(testPerson);
    }

    @Test
    @DisplayName("Test delete person - not found")
    void testDeletePersonNotFound() {
        // Arrange
        when(personRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = personService.deletePerson(999L);

        // Assert
        assertFalse(result);
        verify(personRepository, times(1)).findById(999L);
        verify(personRepository, never()).delete(any(Person.class));
    }

    @Test
    @DisplayName("Test PersonService constructor")
    void testConstructor() {
        PersonRepository mockRepo = mock(PersonRepository.class);
        PersonService service = new PersonService(mockRepo);
        assertNotNull(service);
    }
}
