package ru.practicum.ewm.stats.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.stats.exception.ValidationException;
import ru.practicum.ewm.stats.mapper.HitMapper;
import ru.practicum.ewm.stats.model.Hit;
import ru.practicum.ewm.stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class HitService {

    private final HitRepository hitRepository;

    public HitService(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    public void saveHit(EndpointHit endpointHit) {
        Hit hit = HitMapper.toHit(endpointHit);
        hitRepository.save(hit);
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала стастистики не может быть позже даты окончания статистики");
        }

        if (uris != null) {
            uris = uris.stream()
                    .filter(uri -> !uri.isBlank())
                    .toList();

            if (uris.isEmpty()) {
                uris = null;
            }
        }
        List<Object[]> rows;
        if (unique != null && unique) {
            rows = hitRepository.findUniqueStats(start, end, uris);
        } else {
            rows = hitRepository.findStats(start, end, uris);
        }
        List<ViewStats> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(HitMapper.toViewStats(row));
        }
        return result;
    }

}
