package com.example.github.demo.controller;

import com.example.github.demo.model.Person;
import com.example.github.demo.service.PersonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonController.class)
class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PersonService personService;

    @Autowired
    private ObjectMapper objectMapper;

    private Person testPerson;
    private Person secondPerson;

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

        secondPerson = new Person();
        secondPerson.setId(2L);
        secondPerson.setFirstName("Jane");
        secondPerson.setLastName("Smith");
        secondPerson.setEmail("jane.smith@example.com");
        secondPerson.setPhoneNumber("098-765-4321");
        secondPerson.setAddress("456 Elm St");
        secondPerson.setAge(25);
    }

    @Test
    @DisplayName("GET /api/persons - Get all persons")
    void testGetAllPersons() throws Exception {
        // Arrange
        List<Person> persons = Arrays.asList(testPerson, secondPerson);
        when(personService.getAllPersons()).thenReturn(persons);

        // Act & Assert
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[0].phoneNumber").value("123-456-7890"))
                .andExpect(jsonPath("$[0].address").value("123 Main St"))
                .andExpect(jsonPath("$[0].age").value(30))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].lastName").value("Smith"));

        verify(personService, times(1)).getAllPersons();
    }

    @Test
    @DisplayName("GET /api/persons - Empty list")
    void testGetAllPersonsEmpty() throws Exception {
        // Arrange
        when(personService.getAllPersons()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(personService, times(1)).getAllPersons();
    }

    @Test
    @DisplayName("GET /api/persons/{id} - Get person by ID - found")
    void testGetPersonByIdFound() throws Exception {
        // Arrange
        when(personService.getPersonById(1L)).thenReturn(Optional.of(testPerson));

        // Act & Assert
        mockMvc.perform(get("/api/persons/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("123-456-7890"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andExpect(jsonPath("$.age").value(30));

        verify(personService, times(1)).getPersonById(1L);
    }

    @Test
    @DisplayName("GET /api/persons/{id} - Get person by ID - not found")
    void testGetPersonByIdNotFound() throws Exception {
        // Arrange
        when(personService.getPersonById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/persons/999"))
                .andExpect(status().isNotFound());

        verify(personService, times(1)).getPersonById(999L);
    }

    @Test
    @DisplayName("POST /api/persons - Create person")
    void testCreatePerson() throws Exception {
        // Arrange
        Person newPerson = new Person();
        newPerson.setFirstName("New");
        newPerson.setLastName("Person");
        newPerson.setEmail("new.person@example.com");
        newPerson.setPhoneNumber("111-222-3333");
        newPerson.setAddress("789 Oak St");
        newPerson.setAge(28);

        Person savedPerson = new Person();
        savedPerson.setId(3L);
        savedPerson.setFirstName("New");
        savedPerson.setLastName("Person");
        savedPerson.setEmail("new.person@example.com");
        savedPerson.setPhoneNumber("111-222-3333");
        savedPerson.setAddress("789 Oak St");
        savedPerson.setAge(28);

        when(personService.createPerson(any(Person.class))).thenReturn(savedPerson);

        // Act & Assert
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPerson)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("Person"))
                .andExpect(jsonPath("$.email").value("new.person@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("111-222-3333"))
                .andExpect(jsonPath("$.address").value("789 Oak St"))
                .andExpect(jsonPath("$.age").value(28));

        verify(personService, times(1)).createPerson(any(Person.class));
    }

    @Test
    @DisplayName("POST /api/persons - Create person with invalid JSON")
    void testCreatePersonWithInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(personService, never()).createPerson(any(Person.class));
    }

    @Test
    @DisplayName("PUT /api/persons/{id} - Update person - found")
    void testUpdatePersonFound() throws Exception {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setFirstName("Updated");
        updatedPerson.setLastName("Person");
        updatedPerson.setEmail("updated.person@example.com");
        updatedPerson.setPhoneNumber("555-555-5555");
        updatedPerson.setAddress("999 Pine St");
        updatedPerson.setAge(35);

        Person savedPerson = new Person();
        savedPerson.setId(1L);
        savedPerson.setFirstName("Updated");
        savedPerson.setLastName("Person");
        savedPerson.setEmail("updated.person@example.com");
        savedPerson.setPhoneNumber("555-555-5555");
        savedPerson.setAddress("999 Pine St");
        savedPerson.setAge(35);

        when(personService.updatePerson(eq(1L), any(Person.class))).thenReturn(Optional.of(savedPerson));

        // Act & Assert
        mockMvc.perform(put("/api/persons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Person"))
                .andExpect(jsonPath("$.email").value("updated.person@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("555-555-5555"))
                .andExpect(jsonPath("$.address").value("999 Pine St"))
                .andExpect(jsonPath("$.age").value(35));

        verify(personService, times(1)).updatePerson(eq(1L), any(Person.class));
    }

    @Test
    @DisplayName("PUT /api/persons/{id} - Update person - not found")
    void testUpdatePersonNotFound() throws Exception {
        // Arrange
        Person updatedPerson = new Person();
        updatedPerson.setFirstName("Updated");
        updatedPerson.setLastName("Person");

        when(personService.updatePerson(eq(999L), any(Person.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/persons/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isNotFound());

        verify(personService, times(1)).updatePerson(eq(999L), any(Person.class));
    }

    @Test
    @DisplayName("PUT /api/persons/{id} - Update person with invalid JSON")
    void testUpdatePersonWithInvalidJson() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/persons/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(personService, never()).updatePerson(anyLong(), any(Person.class));
    }

    @Test
    @DisplayName("DELETE /api/persons/{id} - Delete person - found")
    void testDeletePersonFound() throws Exception {
        // Arrange
        when(personService.deletePerson(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/persons/1"))
                .andExpect(status().isNoContent());

        verify(personService, times(1)).deletePerson(1L);
    }

    @Test
    @DisplayName("DELETE /api/persons/{id} - Delete person - not found")
    void testDeletePersonNotFound() throws Exception {
        // Arrange
        when(personService.deletePerson(999L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/persons/999"))
                .andExpect(status().isNotFound());

        verify(personService, times(1)).deletePerson(999L);
    }

    @Test
    @DisplayName("Test PersonController error handling - Service exception")
    void testServiceException() throws Exception {
        // Arrange
        when(personService.getAllPersons()).thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isInternalServerError());

        verify(personService, times(1)).getAllPersons();
    }

    @Test
    @DisplayName("Test invalid HTTP method")
    void testInvalidHttpMethod() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/persons/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("Test malformed URL path")
    void testMalformedUrlPath() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/persons/not-a-number"))
                .andExpect(status().isBadRequest());
    }
}
