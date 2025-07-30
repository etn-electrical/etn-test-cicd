package com.example.github.demo.integration;

import com.example.github.demo.model.Person;
import com.example.github.demo.repository.PersonRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PersonIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Person testPerson;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        
        testPerson = new Person();
        testPerson.setFirstName("Integration");
        testPerson.setLastName("Test");
        testPerson.setEmail("integration.test@example.com");
        testPerson.setPhoneNumber("555-123-4567");
        testPerson.setAddress("Integration Test Street");
        testPerson.setAge(25);
    }

    @Test
    @DisplayName("Integration Test: Complete CRUD operations flow")
    void testCompleteCrudFlow() throws Exception {
        // 1. Initially no persons
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // 2. Create a person
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPerson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Integration"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.email").value("integration.test@example.com"));

        // 3. Verify person was created in database
        assertEquals(1, personRepository.count());
        Person createdPerson = personRepository.findAll().get(0);
        assertNotNull(createdPerson.getId());
        assertEquals("Integration", createdPerson.getFirstName());

        // 4. Get all persons - should have 1
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].firstName").value("Integration"));

        // 5. Get person by ID
        Long personId = createdPerson.getId();
        mockMvc.perform(get("/api/persons/" + personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId))
                .andExpect(jsonPath("$.firstName").value("Integration"));

        // 6. Update the person
        Person updatedPerson = new Person();
        updatedPerson.setFirstName("Updated");
        updatedPerson.setLastName("Person");
        updatedPerson.setEmail("updated@example.com");
        updatedPerson.setPhoneNumber("999-888-7777");
        updatedPerson.setAddress("Updated Address");
        updatedPerson.setAge(30);

        mockMvc.perform(put("/api/persons/" + personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPerson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.lastName").value("Person"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        // 7. Verify update in database
        Person dbPerson = personRepository.findById(personId).orElse(null);
        assertNotNull(dbPerson);
        assertEquals("Updated", dbPerson.getFirstName());
        assertEquals("Person", dbPerson.getLastName());
        assertEquals("updated@example.com", dbPerson.getEmail());

        // 8. Delete the person
        mockMvc.perform(delete("/api/persons/" + personId))
                .andExpect(status().isNoContent());

        // 9. Verify deletion
        assertEquals(0, personRepository.count());
        assertFalse(personRepository.findById(personId).isPresent());

        // 10. Try to get deleted person - should return 404
        mockMvc.perform(get("/api/persons/" + personId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test: Multiple persons management")
    void testMultiplePersonsManagement() throws Exception {
        // Create multiple persons
        Person person1 = new Person();
        person1.setFirstName("Person");
        person1.setLastName("One");
        person1.setEmail("person1@example.com");
        person1.setAge(20);

        Person person2 = new Person();
        person2.setFirstName("Person");
        person2.setLastName("Two");
        person2.setEmail("person2@example.com");
        person2.setAge(30);

        Person person3 = new Person();
        person3.setFirstName("Person");
        person3.setLastName("Three");
        person3.setEmail("person3@example.com");
        person3.setAge(40);

        // Create all persons
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(person3)))
                .andExpect(status().isCreated());

        // Verify all were created
        assertEquals(3, personRepository.count());

        // Get all persons
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        // Delete one person
        Person firstPerson = personRepository.findAll().get(0);
        mockMvc.perform(delete("/api/persons/" + firstPerson.getId()))
                .andExpect(status().isNoContent());

        // Verify count reduced
        assertEquals(2, personRepository.count());

        // Get all persons again
        mockMvc.perform(get("/api/persons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Integration Test: Error scenarios")
    void testErrorScenarios() throws Exception {
        // Try to get non-existent person
        mockMvc.perform(get("/api/persons/999"))
                .andExpect(status().isNotFound());

        // Try to update non-existent person
        Person updatePerson = new Person();
        updatePerson.setFirstName("Update");
        updatePerson.setLastName("Test");

        mockMvc.perform(put("/api/persons/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePerson)))
                .andExpect(status().isNotFound());

        // Try to delete non-existent person
        mockMvc.perform(delete("/api/persons/999"))
                .andExpect(status().isNotFound());

        // Try to create person with invalid JSON
        mockMvc.perform(post("/api/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());

        // Verify no persons were created during error tests
        assertEquals(0, personRepository.count());
    }

    @Test
    @DisplayName("Integration Test: Database persistence")
    void testDatabasePersistence() {
        // Save person directly to repository
        Person savedPerson = personRepository.save(testPerson);
        assertNotNull(savedPerson.getId());
        assertEquals("Integration", savedPerson.getFirstName());

        // Find by ID
        Person foundPerson = personRepository.findById(savedPerson.getId()).orElse(null);
        assertNotNull(foundPerson);
        assertEquals(savedPerson.getId(), foundPerson.getId());
        assertEquals("Integration", foundPerson.getFirstName());

        // Update person
        foundPerson.setFirstName("Updated Integration");
        Person updatedPerson = personRepository.save(foundPerson);
        assertEquals("Updated Integration", updatedPerson.getFirstName());

        // Delete person
        personRepository.delete(updatedPerson);
        assertEquals(0, personRepository.count());
        assertFalse(personRepository.findById(savedPerson.getId()).isPresent());
    }

    @Test
    @DisplayName("Integration Test: Application context loads")
    void testApplicationContextLoads() {
        assertNotNull(mockMvc);
        assertNotNull(personRepository);
        assertNotNull(objectMapper);
    }
}
