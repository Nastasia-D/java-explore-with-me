package ru.practicum.main.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.dto.CompilationDto;
import ru.practicum.main.dto.EventShortDto;
import ru.practicum.main.dto.NewCompilationDto;
import ru.practicum.main.dto.UpdateCompilationRequest;
import ru.practicum.main.model.Compilation;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CompilationMapper {

    public static CompilationDto toDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setPinned(compilation.getPinned());
        compilationDto.setTitle(compilation.getTitle());

        List<EventShortDto> events = compilation.getEvents() == null
                ? Collections.emptyList()
                : compilation.getEvents().stream()
                .map(EventMapper::toShortDto)
                .toList();

        compilationDto.setEvents(events);
        return compilationDto;
    }

    public static Compilation toCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto == null) {
            return null;
        }

        Compilation compilation = new Compilation();
        compilation.setPinned(newCompilationDto.getPinned());
        compilation.setTitle(newCompilationDto.getTitle());
        return compilation;
    }

    public static void updateCompilationFromRequest(UpdateCompilationRequest request, Compilation compilation) {

        if (request == null || compilation == null) {
            return;
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
    }
}
