package com.example.restapidemo.events;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class EventValidator implements Validator {

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.isAssignableFrom(EventDto.class);
    }

    @Override
    public void validate(Object object, Errors errors) {
        EventDto eventDto = (EventDto) object;
        if (eventDto.getBasePrice() > eventDto.getMaxPrice() && eventDto.getMaxPrice() != 0) {
            errors.rejectValue(
                    "basePrice",
                    "invalid.basePrice",
                    new Object[]{eventDto.getBasePrice()},
                    "base price is wrong");
            errors.rejectValue(
                    "maxPrice",
                    "invalid.maxPrice",
                    new Object[]{eventDto.getBasePrice()},
                    "max price is wrong");
        }

        LocalDateTime endEventDateTime = eventDto.getEndEventDateTime();
        if (endEventDateTime.isBefore(eventDto.getBeginEventDateTime()) ||
                endEventDateTime.isBefore(eventDto.getCloseEnrollmentDateTime()) ||
                endEventDateTime.isBefore(eventDto.getBeginEnrollmentDateTime())) {
            errors.rejectValue(
                    "endEventDateTime",
                    "invalid.endEventDateTime",
                    new Object[]{eventDto.getEndEventDateTime()},
                    "endEventDateTime is wrong");
        }
    }
}
