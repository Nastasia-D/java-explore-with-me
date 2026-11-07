package ru.practicum.main.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.model.CommentStatus;
import ru.practicum.main.service.CommentService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminCommentController {

    private final CommentService commentService;

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("Запрос на удаление комментария с id {}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto moderateCommentByAdmin(@PathVariable Long commentId, @RequestParam CommentStatus status) {
        log.info("Запрос на модерацию комментария с id {}", commentId);
        return commentService.moderateCommentByAdmin(commentId, status);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getCommentsAdminByEvent(@PathVariable Long eventId, @RequestParam(defaultValue = "0") @Min(0) int from,
                                                    @RequestParam(defaultValue = "10")int size,
                                                    @RequestParam(required = false) CommentStatus status) {
        log.info("Запрос на получение списка комментариев со статусом {}", status);
        return commentService.getCommentsAdminByEvent(eventId, from, size, status);
    }
}
