package com.example.restapidemo.events;

import com.example.restapidemo.common.BaseControllerTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.stream.IntStream;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventControllerTest extends BaseControllerTest {

    @Autowired
    EventRepository eventRepository;

    @Test
    @DisplayName("이벤트_생성_성공")
    void createEvent() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 12, 11, 7))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 10, 13, 11, 7))
                .beginEventDateTime(LocalDateTime.of(2020, 10, 14, 11, 7))
                .endEventDateTime(LocalDateTime.of(2020, 10, 15, 11, 7))
                .basePrice(0)
                .maxPrice(0)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        ResultActions resultActions = mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andExpect(header().string("Content-Type", "application/hal+json"))
                .andExpect(jsonPath("id").value(Matchers.not(100)))
                .andExpect(jsonPath("free").value(true))
                .andExpect(jsonPath("offline").value(true))
                .andExpect(jsonPath("eventStatus").value(EventStatus.DRAFT.name()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists())
                ;

        resultActions.andDo(document("create-event",
                links(
                        linkWithRel("self").description("link to self"),
                        linkWithRel("query-events").description("link to query events"),
                        linkWithRel("update-event").description("link to update an existing"),
                        linkWithRel("profile").description("profile")
                ),
                requestHeaders(
                        headerWithName(HttpHeaders.ACCEPT).description("accept header"),
                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                ),
                requestFields(
                        fieldWithPath("name").description("name of new event"),
                        fieldWithPath("description").description("description of new event"),
                        fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                        fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime"),
                        fieldWithPath("beginEventDateTime").description("beginEventDateTime"),
                        fieldWithPath("endEventDateTime").description("endEventDateTime"),
                        fieldWithPath("location").description("location"),
                        fieldWithPath("basePrice").description("basePrice"),
                        fieldWithPath("maxPrice").description("maxPrice"),
                        fieldWithPath("limitOfEnrollment").description("limitOfEnrollment")
                ),
                responseHeaders(
                        headerWithName(HttpHeaders.LOCATION).description("response header"),
                        headerWithName(HttpHeaders.CONTENT_TYPE).description("content type")
                ),
//                relaxedResponseFields(
                responseFields(
                        fieldWithPath("id").description("id of new event"),
                        fieldWithPath("name").description("name of new event"),
                        fieldWithPath("description").description("description of new event"),
                        fieldWithPath("beginEnrollmentDateTime").description("date time of begin of new event"),
                        fieldWithPath("closeEnrollmentDateTime").description("closeEnrollmentDateTime"),
                        fieldWithPath("beginEventDateTime").description("beginEventDateTime"),
                        fieldWithPath("endEventDateTime").description("endEventDateTime"),
                        fieldWithPath("location").description("location"),
                        fieldWithPath("basePrice").description("basePrice"),
                        fieldWithPath("maxPrice").description("maxPrice"),
                        fieldWithPath("limitOfEnrollment").description("limitOfEnrollment"),
                        fieldWithPath("free").description("it tells tis this event is free or not"),
                        fieldWithPath("offline").description("it tells tis this event is offline or not"),
                        fieldWithPath("eventStatus").description("event status"),
                        fieldWithPath("_links.self.href").description("link to self"),
                        fieldWithPath("_links.query-events.href").description("link to query event list"),
                        fieldWithPath("_links.update-event.href").description("link to update existing event"),
                        fieldWithPath("_links.profile.href").description("link to profile")

                )
        ));
    }

    @Test
    @DisplayName("이벤트_생성_실패_입력값_오류")
    void createEventBadRequestWrongInput() throws Exception {
        EventDto event = EventDto.builder()
                .name("Spring")
                .description("REST API Development with Spring")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 12, 11, 7))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 9, 13, 11, 7))
                .beginEventDateTime(LocalDateTime.of(2020, 9, 14, 11, 7))
                .endEventDateTime(LocalDateTime.of(2020, 8, 15, 11, 7))
                .basePrice(10000)
                .maxPrice(200)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors[0].objectName").exists())
                .andExpect(jsonPath("errors[0].defaultMessage").exists())
                .andExpect(jsonPath("errors[0].code").exists())
                .andExpect(jsonPath("_links.index").exists())
        ;
    }

    @Test
    void queryEvents() throws Exception {
        IntStream.range(0, 30).forEach(this::generateEvent);

        ResultActions result = mockMvc.perform(get("/api/events")
                .param("page", "1")
                .param("size", "10")
                .param("sort", "id,DESC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("page").exists())
                .andExpect(jsonPath("_embedded.eventList[0]").exists())
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists());

        result.andDo(document("query-events"));
    }

    @Test
    void getEvent() throws Exception {
        Event event = generateEvent(1);

        mockMvc.perform(get("/api/events/{id}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(event.getName()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
        ;
    }

    @Test
    void updateEvent() throws Exception {
        Event event = generateEvent(1);

        EventDto updateEvent = EventDto.builder()
                .name("updated events")
                .build();

        mockMvc.perform(put("/api/events/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateEvent)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("name").value(updateEvent.getName()))
                .andExpect(jsonPath("_links.self").exists())
                .andExpect(jsonPath("_links.profile").exists())
                ;
    }

    private Event generateEvent(int index) {
        Event event = Event.builder()
                .id(index)
                .name("Event" + index)
                .description("Test Event")
                .beginEnrollmentDateTime(LocalDateTime.of(2020, 10, 12, 11, 7))
                .closeEnrollmentDateTime(LocalDateTime.of(2020, 10, 13, 11, 7))
                .beginEventDateTime(LocalDateTime.of(2020, 10, 14, 11, 7))
                .endEventDateTime(LocalDateTime.of(2020, 10, 15, 11, 7))
                .basePrice(0)
                .maxPrice(0)
                .limitOfEnrollment(100)
                .location("강남역 D2 스타텁 팩토리")
                .build();
        return eventRepository.save(event);
    }
}
