package ru.practicum.main.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.service.CommentService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Запрос на создание комментария от пользователя {} на событие {}", userId, eventId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {
        log.info("Запрос на удаление комментария с id {}", commentId);
        commentService.deleteComment(userId, commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId, @PathVariable Long commentId, @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("Запрос на изменение комментария с id {} от пользователя с id {}", commentId, userId);
        return commentService.updateComment(userId, commentId, newCommentDto);
    }
}
