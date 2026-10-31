package ru.practicum.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.dto.*;
import ru.practicum.main.model.Event;
import ru.practicum.main.model.Location;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(CategoryMapper.toDto(event.getCategory()));
        eventShortDto.setEventDate(event.getEventDate().format(FORMATTER));
        eventShortDto.setId(event.getId());
        eventShortDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        eventShortDto.setPaid(event.getPaid());
        eventShortDto.setTitle(event.getTitle());
        return eventShortDto;

    }

    public static EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }

        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.toDto(event.getCategory()));
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setId(event.getId());
        eventFullDto.setInitiator(UserMapper.toShortDto(event.getInitiator()));
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setCreatedOn(event.getCreatedOn().format(FORMATTER));
        eventFullDto.setEventDate(event.getEventDate().format(FORMATTER));
        eventFullDto.setPublishedOn(event.getPublishedOn() != null ? event.getPublishedOn().format(FORMATTER) : null);
        eventFullDto.setLocation(toLocationDto(event.getLocation()));
        eventFullDto.setPaid(event.getPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setRequestModeration(event.getRequestModeration());
        eventFullDto.setState(event.getState().name());
        return eventFullDto;
    }

    public static Event toEvent(NewEventDto newEventDto) {
        if (newEventDto == null) {
            return null;
        }

        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setPaid(newEventDto.getPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.getRequestModeration());
        event.setTitle(newEventDto.getTitle());
        event.setLocation(toLocation(newEventDto.getLocation()));
        event.setEventDate(newEventDto.getEventDate());
        return event;
    }

    public static void updateEventFromUserRequest(UpdateEventUserRequest request, Event event) {
        if (request == null || event == null) {
            return;
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null) {
            event.setLocation(toLocation(request.getLocation()));
        }

    }

    public static void updateEventFromAdminRequest(UpdateEventAdminRequest request, Event event) {
        if (request == null || event == null) {
            return;
        }

        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }

        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }

        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }

        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }

        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }

        if (request.getLocation() != null) {
            event.setLocation(toLocation(request.getLocation()));
        }
    }

    private static LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        return locationDto;
    }

    private static Location toLocation(LocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }

        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }
}
