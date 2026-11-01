package ru.practicum.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.EventFullDto;
import ru.practicum.main.dto.UpdateEventAdminRequest;
import ru.practicum.main.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long eventId, @Valid @RequestBody UpdateEventAdminRequest request) {
        log.info("Запрос администратора на обновление события с id {}", eventId);
        return eventService.updateEventByAdmin(eventId, request);
    }

    @GetMapping
    public List<EventFullDto> searchAdminEvents(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<String> states,
                                                @RequestParam(required = false) List<Long> categories, @RequestParam(required = false)
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                    LocalDateTime rangeEnd, @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос администратора на поиск событий");
        return eventService.searchAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }
}
