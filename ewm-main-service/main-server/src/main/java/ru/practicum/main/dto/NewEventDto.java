package ru.practicum.main.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank(message = "Аннотация не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов")
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Дата события не может быть пустой")
    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private LocationDto location;
    private Boolean paid = false;

    @PositiveOrZero(message = "Лимит участников должен быть больше или равен нулю")
    private Integer participantLimit = 0;
    private Boolean requestModeration = true;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 3, max = 120, message = "Название должно содержать от 3 до 120 символов")
    private String title;
}
