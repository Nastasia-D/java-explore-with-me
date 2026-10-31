package ru.practicum.main.service;

import client.StatsClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.main.dto.*;
import ru.practicum.main.error.ConflictException;
import ru.practicum.main.error.NotFoundException;
import ru.practicum.main.mapper.EventMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final ObjectMapper objectMapper;

    public EventService(EventRepository eventRepository, CategoryRepository categoryRepository, UserRepository userRepository,
                        ParticipationRequestRepository participationRequestRepository, StatsClient statsClient, ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.participationRequestRepository = participationRequestRepository;
        this.statsClient = statsClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Long categoryId = newEventDto.getCategory();
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));

        if (LocalDateTime.parse(newEventDto.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("Дата события должна быть не раньше чем через два часа");
        }
        Event event = EventMapper.toEvent(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(State.PENDING);
        Event savedEvent = eventRepository.save(event);
        return toFullDtoWithStats(savedEvent);
    }


    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<Event> events = eventRepository.findEventsByInitiator(userId, from, size);
        List<EventShortDto> eventShortDtos = new ArrayList<>();

        for (Event event : events) {
            EventShortDto eventShortDto = EventMapper.toShortDto(event);

            long confirmedRequests = participationRequestRepository.countByEvent_IdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            eventShortDto.setConfirmedRequests(confirmedRequests);
            Map<String, Long> views = getViews(List.of(event));
            eventShortDto.setViews(views.getOrDefault("/events/" + event.getId(), 0L));
            eventShortDtos.add(eventShortDto);
        }
        return eventShortDtos;
    }


    public EventFullDto getUserEvent(Long userId, Long eventId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("У пользователя с id " + userId + " нет события с id " + eventId);
        }

        return toFullDtoWithStats(event);
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("У пользователя с id " + userId + " нет события с id " + eventId);
        }

        if (request.getCategory() != null) {
            Long categoryId = request.getCategory();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
            event.setCategory(category);
        }

        if (request.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(
                    request.getEventDate(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("Дата события должна быть не раньше чем через два часа");
            }
        }

        if (event.getState() != State.PENDING && event.getState() != State.CANCELED) {
            throw new ConflictException("Можно изменять только события в состоянии PENDING или CANCELED");
        }

        EventMapper.updateEventFromUserRequest(request, event);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case SEND_TO_REVIEW -> event.setState(State.PENDING);
                case CANCEL_REVIEW -> event.setState(State.CANCELED);
                default -> throw new ConflictException("Недопустимое действие с состоянием события: " + request.getStateAction());
            }
        }

        Event savedEvent = eventRepository.save(event);
        return toFullDtoWithStats(savedEvent);
    }

    private Map<String, Long> getViews(List<Event> events) {
        if (events.isEmpty()) {
            return Map.of();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        LocalDateTime minCreatedOn = events.stream()
                .map(event -> event.getCreatedOn())
                .filter(date -> date != null)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(1));

        List<ViewStats> stats = objectMapper.convertValue(
                statsClient.stats(minCreatedOn, LocalDateTime.now().plusSeconds(1), uris, true).getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, ViewStats.class));

        Map<String, Long> views = new HashMap<>();
        if (stats != null) {
            for (ViewStats viewStat : stats) {
                views.put(viewStat.getUri(), viewStat.getHits());
            }
        }
        return views;
    }

    private long getConfirmedRequests(Long eventId) {
        return participationRequestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    private EventFullDto toFullDtoWithStats(Event event) {
        EventFullDto dto = EventMapper.toFullDto(event);
        dto.setConfirmedRequests(getConfirmedRequests(event.getId()));
        Map<String, Long> views = getViews(List.of(event));
        dto.setViews(views.getOrDefault("/events/" + event.getId(), 0L));
        return dto;
    }


    public List<EventShortDto> searchPublishedEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                     LocalDateTime rangeEnd, boolean onlyAvailable, String sort, int from, int size) {
        if (from < 0 || size < 1) {
            throw new IllegalArgumentException("from должен быть не меньше 0, size — не меньше 1");
        }

        if (text != null && (text.isBlank() || text.length() > 7000)) {
            throw new IllegalArgumentException("text должен содержать от 1 до 7000 символов");
        }

        if (sort != null && !sort.equals("EVENT_DATE") && !sort.equals("VIEWS")) {
            throw new IllegalArgumentException("Недопустимое значение sort: " + sort);
        }

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();

        if (rangeEnd != null && rangeEnd.isBefore(start)) {
            throw new IllegalArgumentException("rangeEnd не может быть раньше начала поиска"
            );
        }

        boolean filterCategories = categories != null && !categories.isEmpty();
        List<Long> categoryIds = filterCategories ? categories : List.of(-1L);

        List<Event> events = new ArrayList<>(eventRepository.searchPublishedEvents(text, filterCategories, categoryIds, paid, start, rangeEnd));

        if (onlyAvailable) {
            events.removeIf(event -> event.getParticipantLimit() != 0 && participationRequestRepository
                    .countByEvent_IdAndStatus(event.getId(), RequestStatus.CONFIRMED) >= event.getParticipantLimit());
        }

        Map<String, Long> views = getViews(events);

        if ("EVENT_DATE".equals(sort)) {
            events.sort(Comparator.comparing(Event::getEventDate)
                    .thenComparing(Event::getId));
        } else if ("VIEWS".equals(sort)) {
            events.sort(Comparator
                    .comparingLong((Event event) -> views.getOrDefault("/events/" + event.getId(), 0L))
                    .reversed()
                    .thenComparing(Event::getId));
        }

        int startIndex = Math.min(from, events.size());
        int endIndex = (int) Math.min((long) startIndex + size, events.size());

        List<Event> page = events.subList(startIndex, endIndex);
        List<EventShortDto> result = new ArrayList<>();

        for (Event event : page) {
            EventShortDto dto = EventMapper.toShortDto(event);
            dto.setConfirmedRequests(participationRequestRepository
                            .countByEvent_IdAndStatus(event.getId(), RequestStatus.CONFIRMED));

            dto.setViews(views.getOrDefault("/events/" + event.getId(), 0L));
            result.add(dto);
        }

        return result;
    }

    public List<EventFullDto> searchAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {

        boolean filterUsers = users != null && !users.isEmpty();
        boolean filterStates = states != null && !states.isEmpty();
        boolean filterCategories = categories != null && !categories.isEmpty();

        if (!filterUsers) {
            users = new ArrayList<>();
            users.add(-1L);
        }

        if (!filterStates) {
            states = new ArrayList<>();
            states.add("__NO_STATE__");
        }

        if (!filterCategories) {
            categories = new ArrayList<>();
            categories.add(-1L);
        }

        List<Event> events = eventRepository.searchAdminEvents(filterUsers, users, filterStates, states, filterCategories, categories, rangeStart, rangeEnd, from, size);
        List<EventFullDto> result = new ArrayList<>();

        for (Event event : events) {
            result.add(toFullDtoWithStats(event));
        }

        return result;
    }

    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (request.getCategory() != null) {
            Long categoryId = request.getCategory();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Категория с id " + categoryId + " не найдена"));
            event.setCategory(category);
        }

        if (request.getEventDate() != null) {
            LocalDateTime eventDate = LocalDateTime.parse(request.getEventDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new IllegalArgumentException("Дата события должна быть не раньше чем через один час");
            }
        }

        EventMapper.updateEventFromAdminRequest(request, event);

        if (request.getStateAction() != null) {
            switch (request.getStateAction()) {
                case PUBLISH_EVENT -> {
                    if (event.getState() != State.PENDING) {
                        throw new ConflictException("Опубликовать можно только событие в состоянии PENDING");
                    }

                    if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                        throw new ConflictException("Дата события должна быть не раньше чем через один час");
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                }
                case REJECT_EVENT -> {
                    if (event.getState() != State.PENDING) {
                        throw new ConflictException("Отклонить можно только событие в состоянии PENDING");
                    }
                    event.setState(State.CANCELED);
                }
                default -> throw new ConflictException("Недопустимое действие с состоянием события: " + request.getStateAction());
            }
        }

        Event savedEvent = eventRepository.save(event);
        return toFullDtoWithStats(savedEvent);

    }

    public EventFullDto getPublishedEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() != State.PUBLISHED) {
            throw new NotFoundException("Опубликованное событие с id " + eventId + " не найдено");
        }
        return toFullDtoWithStats(event);
    }
}
