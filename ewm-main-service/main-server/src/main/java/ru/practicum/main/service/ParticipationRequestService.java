package ru.practicum.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.dto.ParticipationRequestDto;
import ru.practicum.main.error.ConflictException;
import ru.practicum.main.error.NotFoundException;
import ru.practicum.main.mapper.ParticipationRequestMapper;
import ru.practicum.main.model.*;
import ru.practicum.main.repository.EventRepository;
import ru.practicum.main.repository.ParticipationRequestRepository;
import ru.practicum.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParticipationRequestService {

    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ParticipationRequestService(ParticipationRequestRepository requestRepository, UserRepository userRepository,
                                       EventRepository eventRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ParticipationRequest> requests = requestRepository.findAllByRequester_IdOrderByIdAsc(userId);

        List<ParticipationRequestDto> requestsDto = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            ParticipationRequestDto dto = ParticipationRequestMapper.toDto(request);
            requestsDto.add(dto);
        }
        return requestsDto;
    }

    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (requester.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Нельзя подать заявку на собственное событие");
        }

        if (event.getState() != State.PUBLISHED) {
            throw new ConflictException("Нельзя подать заявку на неопубликованное событие");
        }

        if (event.getEventDate().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Нельзя подать заявку на прошедшее событие");
        }

        if (requestRepository.existsByRequester_IdAndEvent_Id(userId, eventId)) {
            throw new ConflictException("Пользователь уже подавал заявку на это событие");
        }

        long confirmedRequests = requestRepository.countByEvent_IdAndStatus(eventId, RequestStatus.CONFIRMED);

        if (event.getParticipantLimit() > 0 && confirmedRequests >= event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников события");
        }

        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(requester);

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        ParticipationRequest savedRequest = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(savedRequest);
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id " + requestId + " не найдена"));

        if (!request.getRequester().getId().equals(userId)) {
            throw new ConflictException("Нельзя отменить чужую заявку");
        }

        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest savedRequest = requestRepository.save(request);
        return ParticipationRequestMapper.toDto(savedRequest);
    }

    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Получить заявки может только инициатор события");
        }

        List<ParticipationRequest> requests = requestRepository.findAllByEvent_IdOrderByIdAsc(eventId);
        List<ParticipationRequestDto> requestsDto = new ArrayList<>();

        for (ParticipationRequest request : requests) {
            requestsDto.add(ParticipationRequestMapper.toDto(request));
        }
        return requestsDto;
    }


    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id " + eventId + " не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Обрабатывать заявки может только инициатор события");
        }

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            throw new ConflictException("Подтверждение заявок для этого события не требуется");
        }

        RequestStatus newStatus;

        try {
            newStatus = RequestStatus.valueOf(updateRequest.getStatus());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ConflictException("Недопустимый статус заявки");
        }

        if (newStatus != RequestStatus.CONFIRMED && newStatus != RequestStatus.REJECTED) {
            throw new ConflictException("Можно подтвердить или отклонить заявки");
        }

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        long confirmedCount = requestRepository.countByEvent_IdAndStatus(
                eventId, RequestStatus.CONFIRMED);

        for (Long requestId : updateRequest.getRequestIds()) {
            ParticipationRequest request = requestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Заявка с id " + requestId + " не найдена"));

            if (!request.getEvent().getId().equals(eventId)) {
                throw new ConflictException("Заявка не относится к указанному событию");
            }

            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Обрабатывать можно только ожидающие заявки");
            }

            if (newStatus == RequestStatus.CONFIRMED && confirmedCount >= event.getParticipantLimit()) {
                throw new ConflictException("Достигнут лимит подтвержденных заявок");
            }

            request.setStatus(newStatus);
            ParticipationRequest savedRequest = requestRepository.save(request);

            ParticipationRequestDto dto = ParticipationRequestMapper.toDto(savedRequest);

            if (newStatus == RequestStatus.CONFIRMED) {
                confirmedRequests.add(dto);
                confirmedCount++;
            } else {
                rejectedRequests.add(dto);
            }
        }

        if (newStatus == RequestStatus.CONFIRMED && confirmedCount >= event.getParticipantLimit()) {

            List<ParticipationRequest> pendingRequests = requestRepository.findAllByEvent_IdAndStatus(eventId, RequestStatus.PENDING);

            for (ParticipationRequest pendingRequest : pendingRequests) {
                pendingRequest.setStatus(RequestStatus.REJECTED);
                ParticipationRequest savedRequest = requestRepository.save(pendingRequest);
                rejectedRequests.add(ParticipationRequestMapper.toDto(savedRequest));
            }
        }

        result.setConfirmedRequests(confirmedRequests);
        result.setRejectedRequests(rejectedRequests);

        return result;
    }

}
