package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCategory_Id(Long categoryId);

    @Query(value = "SELECT * FROM events " +
                    "WHERE initiator_id = :userId " +
                    "ORDER BY id " +
                    "LIMIT :size OFFSET :from",
            nativeQuery = true)
    List<Event> findEventsByInitiator(@Param("userId") Long userId, @Param("from") int from, @Param("size") int size);

    @Query(value = "SELECT * FROM events " +
            "WHERE state = 'PUBLISHED' " +
            "AND (:text IS NULL OR " +
            "LOWER(annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "ORDER BY id " +
            "LIMIT :size OFFSET :from",
            nativeQuery = true)
    List<Event> searchPublishedEventsByText(@Param("text") String text, @Param("from") int from, @Param("size") int size);

    @Query(value = "SELECT * FROM events " +
            "WHERE (:filterUsers = false OR initiator_id IN (:users)) " +
            "AND (:filterStates = false OR state::text IN (:states)) " +
            "AND (:filterCategories = false OR category_id IN (:categories)) " +
            "AND (CAST(:rangeStart AS timestamp) IS NULL " +
            "OR event_date >= CAST(:rangeStart AS timestamp)) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL " +
            "OR event_date <= CAST(:rangeEnd AS timestamp)) " +
            "ORDER BY event_date " +
            "LIMIT :size OFFSET :from",
            nativeQuery = true)
    List<Event> searchAdminEvents(@Param("filterUsers") boolean filterUsers, @Param("users") List<Long> users,
                                  @Param("filterStates") boolean filterStates, @Param("states") List<String> states,
                                  @Param("filterCategories") boolean filterCategories, @Param("categories") List<Long> categories,
                                  @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd,
                                  @Param("from") int from, @Param("size") int size);


    @Query(value = "SELECT e.* FROM events e " +
            "WHERE e.state = 'PUBLISHED' " +
            "AND (:text IS NULL OR " +
            "LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:filterCategories = false OR e.category_id IN (:categories)) " +
            "AND (CAST(:paid AS boolean) IS NULL OR e.paid = CAST(:paid AS boolean)) " +
            "AND e.event_date >= CAST(:rangeStart AS timestamp) " +
            "AND (CAST(:rangeEnd AS timestamp) IS NULL " +
            "OR e.event_date <= CAST(:rangeEnd AS timestamp)) " +
            "ORDER BY e.id ASC",
            nativeQuery = true)
    List<Event> searchPublishedEvents(@Param("text") String text, @Param("filterCategories") boolean filterCategories,
                                      @Param("categories") List<Long> categories, @Param("paid") Boolean paid,
                                      @Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);


}
