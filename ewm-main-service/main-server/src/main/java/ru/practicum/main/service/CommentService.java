package ru.practicum.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CommentDto;
import ru.practicum.main.dto.NewCommentDto;
import ru.practicum.main.error.ConflictException;
import ru.practicum.main.error.NotFoundException;
import ru.practicum.main.mapper.CommentMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.CommentRepository;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.main.model.CommentStatus.PENDING;
import static ru.practicum.main.model.State.PUBLISHED;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CommentService(CommentRepository commentRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() != PUBLISHED) {
            throw new ConflictException("Нельзя оставлять комментарии к неопубликованным событиям.");
        }

        Comment comment = CommentMapper.mapToComment(newCommentDto, event, initiator);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(savedComment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Пользователь может удалять только свои комментарии.");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));
        commentRepository.delete(comment);
    }

    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (event.getState() != PUBLISHED) {
            throw new NotFoundException("Событие не опубликовано");
        }
        List<Comment> comments = commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED);
        List<CommentDto> commentDtos = new ArrayList<>();
        for (int i = from; i < comments.size() && commentDtos.size() < size; i++) {
            Comment comment = comments.get(i);
            CommentDto commentDto = CommentMapper.mapToCommentDto(comment);
            commentDtos.add(commentDto);
        }
        return commentDtos;
    }

    public List<CommentDto> getCommentsAdminByEvent(Long eventId, int from, int size, CommentStatus status) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        List<Comment> comments;
        if (status == null) {
            comments = commentRepository.findByEventId(eventId);
        } else {
            comments = switch (status) {
                case PENDING -> commentRepository.findByEventIdAndStatus(eventId, PENDING);
                case APPROVED -> commentRepository.findByEventIdAndStatus(eventId, CommentStatus.APPROVED);
                case REJECTED -> commentRepository.findByEventIdAndStatus(eventId, CommentStatus.REJECTED);
            };
        }

        List<CommentDto> commentDtos = new ArrayList<>();
        for (int i = from; i < comments.size() && commentDtos.size() < size; i++) {
            Comment comment = comments.get(i);
            CommentDto commentDto = CommentMapper.mapToCommentDto(comment);
            commentDtos.add(commentDto);
        }
        return commentDtos;
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("У пользователя с id " + userId + " нет комментария с id " + commentId);
        }

        if (newCommentDto.getText() != null && !newCommentDto.getText().isBlank()) {
            comment.setText(newCommentDto.getText());
        }

        Comment saved = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(saved);
    }

    @Transactional
    public CommentDto moderatorAdmin(Long commentId, CommentStatus status) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id " + commentId + " не найден"));

        if (comment.getStatus() != CommentStatus.PENDING) {
            throw new ConflictException("Комментарий с id " + commentId + " уже прошёл модерацию");
        }

        comment.setStatus(status);

        Comment newStatusComment = commentRepository.save(comment);
        return CommentMapper.mapToCommentDto(newStatusComment);
    }
}
