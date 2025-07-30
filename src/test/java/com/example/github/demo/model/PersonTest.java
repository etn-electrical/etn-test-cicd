package com.example.github.demo.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class PersonTest {

    private Person person1;
    private Person person2;
    private Person person3;

    @BeforeEach
    void setUp() {
        person1 = new Person();
        person1.setId(1L);
        person1.setFirstName("John");
        person1.setLastName("Doe");
        person1.setEmail("john.doe@example.com");
        person1.setPhoneNumber("123-456-7890");
        person1.setAddress("123 Main St");
        person1.setAge(30);

        person2 = new Person(2L, "Jane", "Smith", "jane.smith@example.com", 
                           "098-765-4321", "456 Elm St", 25);

        person3 = new Person(1L, "Different", "Person", "john.doe@example.com", 
                           "555-555-5555", "789 Oak St", 35);
    }

    @Test
    @DisplayName("Test Person constructor with all parameters")
    void testAllArgsConstructor() {
        Person person = new Person(1L, "John", "Doe", "john@example.com", 
                                 "123-456-7890", "123 Main St", 30);
        
        assertEquals(1L, person.getId());
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
        assertEquals("john@example.com", person.getEmail());
        assertEquals("123-456-7890", person.getPhoneNumber());
        assertEquals("123 Main St", person.getAddress());
        assertEquals(30, person.getAge());
    }

    @Test
    @DisplayName("Test Person no-args constructor")
    void testNoArgsConstructor() {
        Person person = new Person();
        assertNotNull(person);
        assertNull(person.getId());
        assertNull(person.getFirstName());
    }

    @Test
    @DisplayName("Test all getters and setters")
    void testGettersAndSetters() {
        Person person = new Person();
        
        person.setId(100L);
        person.setFirstName("Test");
        person.setLastName("User");
        person.setEmail("test@example.com");
        person.setPhoneNumber("111-222-3333");
        person.setAddress("Test Address");
        person.setAge(40);

        assertEquals(100L, person.getId());
        assertEquals("Test", person.getFirstName());
        assertEquals("User", person.getLastName());
        assertEquals("test@example.com", person.getEmail());
        assertEquals("111-222-3333", person.getPhoneNumber());
        assertEquals("Test Address", person.getAddress());
        assertEquals(40, person.getAge());
    }

    @Test
    @DisplayName("Test equals method - same object")
    void testEqualsSameObject() {
        assertTrue(person1.equals(person1));
    }

    @Test
    @DisplayName("Test equals method - null object")
    void testEqualsNull() {
        assertFalse(person1.equals(null));
    }

    @Test
    @DisplayName("Test equals method - different class")
    void testEqualsDifferentClass() {
        assertFalse(person1.equals("not a person"));
    }

    @Test
    @DisplayName("Test equals method - same id and email")
    void testEqualsSameIdAndEmail() {
        assertTrue(person1.equals(person3)); // Same id and email
    }

    @Test
    @DisplayName("Test equals method - different objects")
    void testEqualsDifferentObjects() {
        assertFalse(person1.equals(person2));
    }

    @Test
    @DisplayName("Test equals method - null id")
    void testEqualsNullId() {
        Person personNullId1 = new Person(null, "John", "Doe", "john@example.com", "123", "addr", 30);
        Person personNullId2 = new Person(null, "Jane", "Smith", "john@example.com", "456", "addr2", 25);
        
        assertTrue(personNullId1.equals(personNullId2)); // Same email
    }

    @Test
    @DisplayName("Test equals method - null email")
    void testEqualsNullEmail() {
        Person personNullEmail1 = new Person(1L, "John", "Doe", null, "123", "addr", 30);
        Person personNullEmail2 = new Person(1L, "Jane", "Smith", null, "456", "addr2", 25);
        
        assertTrue(personNullEmail1.equals(personNullEmail2)); // Same id
    }

    @Test
    @DisplayName("Test hashCode consistency")
    void testHashCodeConsistency() {
        int hash1 = person1.hashCode();
        int hash2 = person1.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Test hashCode equals contract")
    void testHashCodeEqualsContract() {
        if (person1.equals(person3)) {
            assertEquals(person1.hashCode(), person3.hashCode());
        }
    }

    @Test
    @DisplayName("Test toString method")
    void testToString() {
        String expected = "Person{id=1, firstName='John', lastName='Doe', " +
                         "email='john.doe@example.com', phoneNumber='123-456-7890', " +
                         "address='123 Main St', age=30}";
        assertEquals(expected, person1.toString());
    }

    @Test
    @DisplayName("Test toString with null values")
    void testToStringWithNulls() {
        Person person = new Person();
        String result = person.toString();
        assertTrue(result.contains("Person{"));
        assertTrue(result.contains("id=null"));
    }
}
