package ru.practicum.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.error.NotFoundException;
import ru.practicum.main.mapper.CompilationMapper;
import ru.practicum.main.model.Compilation;
import ru.practicum.main.model.Event;
import ru.practicum.main.repository.CompilationRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public CompilationService(CompilationRepository compilationRepository, EventRepository eventRepository) {
        this.compilationRepository = compilationRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto request) {
        Compilation compilation = CompilationMapper.toCompilation(request);
        compilation.setEvents(getEvents(request.getEvents()));

        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }

        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toDto(saved);
    }

    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));

        CompilationMapper.updateCompilationFromRequest(request, compilation);

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEvents() != null) {
            compilation.setEvents(getEvents(request.getEvents()));
        }
        Compilation saved = compilationRepository.save(compilation);
        return CompilationMapper.toDto(saved);
    }

    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        compilationRepository.delete(compilation);
    }

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        List<Compilation> compilations = compilationRepository.findCompilations(pinned, from, size);
        List<CompilationDto> compilationsDto = new ArrayList<>();
        for (Compilation compilation : compilations) {
            CompilationDto compilationDto = CompilationMapper.toDto(compilation);
            compilationsDto.add(compilationDto);
        }
        return compilationsDto;
    }

    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id " + compId + " не найдена"));
        return CompilationMapper.toDto(compilation);
    }

    private List<Event> getEvents(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Event> eventsById = new HashMap<>();

        for (Event event : eventRepository.findAllById(eventIds)) {
            eventsById.put(event.getId(), event);
        }

        List<Event> events = new ArrayList<>();

        for (Long eventId : eventIds) {
            Event event = eventsById.get(eventId);

            if (event == null) {
                throw new NotFoundException("Событие с id " + eventId + " не найдено");
            }
            events.add(event);
        }
        return events;
    }
}
