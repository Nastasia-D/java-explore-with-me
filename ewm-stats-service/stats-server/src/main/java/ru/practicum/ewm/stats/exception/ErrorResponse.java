package ru.practicum.ewm.stats.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final String error;
    private final String reason;
}
