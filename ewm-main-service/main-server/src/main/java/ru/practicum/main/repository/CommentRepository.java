package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.Comment;
import ru.practicum.main.model.CommentStatus;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);

    List<Comment> findByEventId(Long eventId);
}
