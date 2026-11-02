package ru.practicum.main.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.practicum.main.dto.error.ApiError;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.error("Объект не найден: {}", e.getMessage());
        return new ApiError(List.of(), e.getMessage(), "Объект не найден", HttpStatus.NOT_FOUND.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.error("Конфликт данных: {}", e.getMessage());
        return new ApiError(List.of(), e.getMessage(), "Нарушение условий запроса", HttpStatus.CONFLICT.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String message = "Ошибка валидации входных данных";
        log.warn("Ошибка валидации: {}", errors);
        return new ApiError(errors, message, "Incorrectly made request.", HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {

        log.warn("Некорректное тело запроса: {}", e.getMessage());
        return new ApiError(List.of(), "Некорректное тело запроса", "Incorrectly made request.", HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateTimeParseException(DateTimeParseException e) {
        log.warn("Некорректный формат даты: {}", e.getMessage());
        return new ApiError(List.of(), "Некорректный формат даты", "Incorrectly made request.", HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolationException(ConstraintViolationException e) {

        List<String> errors = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        return new ApiError(errors, "Ошибка валидации параметров запроса", "Incorrectly made request.", HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Некорректные параметры запроса: {}", e.getMessage());
        return new ApiError(List.of(), e.getMessage(), "Incorrectly made request.", HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("Отсутствует обязательный параметр запроса: {}", e.getMessage());
        return new ApiError(List.of(), e.getMessage(), "Incorrectly made request.", HttpStatus.BAD_REQUEST.toString(), LocalDateTime.now().format(FORMATTER));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("Эндпоинт не найден: {}", e.getMessage());
        return new ApiError(List.of(), e.getMessage(), "Объект не найден", HttpStatus.NOT_FOUND.toString(), LocalDateTime.now().format(FORMATTER));
    }
}
