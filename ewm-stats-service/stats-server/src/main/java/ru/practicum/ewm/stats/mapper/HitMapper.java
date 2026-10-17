package ru.practicum.ewm.stats.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.stats.model.Hit;
import ru.practicum.ewm.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HitMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static Hit toHit(EndpointHit endpointHit) {
        Hit hit = new Hit();
        hit.setApp(endpointHit.getApp());
        hit.setUri(endpointHit.getUri());
        hit.setIp(endpointHit.getIp());
        hit.setTimestamp(LocalDateTime.parse(endpointHit.getTimestamp(), FORMATTER));
        return hit;
    }

    public static ViewStats toViewStats(Object[] row) {
        String app = (String) row[0];
        String uri = (String) row[1];
        Long hits = (Long) row[2];
        return new ViewStats(app, uri, hits);
    }
}
