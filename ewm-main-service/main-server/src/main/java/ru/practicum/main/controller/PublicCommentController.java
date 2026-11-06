package ru.practicum.main.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/events/{eventId}/comments")
public class PublicCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getCommentsByEvent(@PathVariable Long eventId, @RequestParam(defaultValue = "0") @Min(0) int from,
                                               @RequestParam(defaultValue = "10")int size) {
        log.info("Запрос на получение списка комментариев к событию с id {}", eventId);
        return commentService.getCommentsByEvent(eventId, from, size);
    }
}
