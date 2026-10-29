package ru.practicum.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.model.ParticipationRequest;
import ru.practicum.main.model.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    long countByEvent_IdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findAllByRequester_IdOrderByIdAsc(Long userId);

    List<ParticipationRequest> findAllByEvent_IdOrderByIdAsc(Long eventId);

    boolean existsByRequester_IdAndEvent_Id(Long userId, Long eventId);

    List<ParticipationRequest> findAllByEvent_IdAndStatus(Long eventId, RequestStatus status);

}
