package ru.practicum.main.controller;

import client.StatsClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/events")
public class PublicEventController {

    private final EventService eventService;
    private final StatsClient statsClient;



    @GetMapping
    public List<EventShortDto> searchEvents(@RequestParam(required = false) String text, @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid, @RequestParam(required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                LocalDateTime rangeEnd, @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                            @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") @Min(0) int from,
                                            @RequestParam(defaultValue = "10") @Min(1) int size, HttpServletRequest request) {
        log.info("Публичный поиск событий с фильтрами");
        saveHit(request);
        return eventService.searchPublishedEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }



    @GetMapping("/{eventId}")
    public EventFullDto getPublishedEvent(@PathVariable Long eventId, HttpServletRequest request) {
        saveHit(request);
        return eventService.getPublishedEvent(eventId);
    }

    private void saveHit(HttpServletRequest request) {
        EndpointHit hit = new EndpointHit(null, "ewm-main-service", request.getRequestURI(),
                request.getRemoteAddr(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        statsClient.hit(hit);
    }

}
