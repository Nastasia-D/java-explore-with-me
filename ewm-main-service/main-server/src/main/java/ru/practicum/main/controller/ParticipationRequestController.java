package ru.practicum.main.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.service.ParticipationRequestService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class ParticipationRequestController {

    private final ParticipationRequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("Запрос заявок пользователя с id {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.info("Запрос на создание заявки пользователя с id {} на событие с id {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId, @PathVariable Long requestId) {
        log.info("Запрос на отмену заявки с id {} пользователя с id {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }
}
