package com.example.restapidemo.events;

import com.example.restapidemo.common.RestDocsConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@Import(RestDocsConfig.class)
public class EventControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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
                .andExpect(jsonPath("_links.query-events").exists())
                .andExpect(jsonPath("_links.update-event").exists());

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
    @DisplayName("이벤트_생성_실패_입력값_초과")
    void createEventBadRequest() throws Exception {
        Event event = Event.builder()
                .id(1000)
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

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event))
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
        ;
    }

//    @Test
//    @DisplayName("이벤트 생성 - 요청 파람 공백")
//    public void creteEvent_Bad_Request_Empty_Input() throws Exception {
//        EventDto eventDto = EventDto.builder().build();
//
//        mockMvc.perform(post("/api/events/")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(eventDto))
//                .accept(MediaTypes.HAL_JSON))
//                .andDo(print())
//                .andExpect(status().isBadRequest())
//        ;
//    }

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
                .andExpect(jsonPath("$[0].objectName").exists())
                .andExpect(jsonPath("$[0].defaultMessage").exists())
                .andExpect(jsonPath("$[0].code").exists())
        ;
    }
}
