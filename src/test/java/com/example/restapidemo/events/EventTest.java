package com.example.restapidemo.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void builder() {
        Event event = Event.builder()
                .name("Inflearn Spring REST API")
                .description("REST API development with Spring")
                .build();
        assertNotNull(event);
    }

    @Test
    void javaBean() {
        Event event = new Event();
        String name = "Inflearn Spring REST API";
        String description = "REST API development with Spring";

        event.setName(name);
        event.setDescription(description);

        assertNotNull(event);
        assertEquals(name, event.getName());
        assertEquals(description, event.getDescription());
    }

}