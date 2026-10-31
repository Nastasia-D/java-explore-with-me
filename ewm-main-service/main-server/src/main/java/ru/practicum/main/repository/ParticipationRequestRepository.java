package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByRequester_IdOrderByIdAsc(Long userId);

    List<ParticipationRequest> findAllByEvent_IdOrderByIdAsc(Long eventId);

    boolean existsByRequester_IdAndEvent_Id(Long userId, Long eventId);

    List<ParticipationRequest> findAllByEvent_IdAndStatus(Long eventId, RequestStatus status);

    @Modifying
    @Query("UPDATE ParticipationRequest r " +
            "SET r.status = :status " +
            "WHERE r.id IN :ids " +
            "AND r.event.id = :eventID")
    int updateRequestsStatus(@Param("ids") List<Long> ids, @Param("status") RequestStatus status, @Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE ParticipationRequest r " +
            "SET r.status = :newStatus " +
            "WHERE r.event.id = :eventId " +
            "AND r.status = :oldStatus")
    int updateRequestsStatusByEventAndStatus(@Param("eventId") Long eventId, @Param("oldStatus") RequestStatus oldStatus,
                                             @Param("newStatus") RequestStatus newStatus);
}
