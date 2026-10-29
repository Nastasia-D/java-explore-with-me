package ru.practicum.main.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {

    @NotEmpty(message = "Список заявок не должен быть пустым")
    private List<Long> requestIds;

    @NotBlank(message = "Статус не должен быть пустым")
    @Pattern(regexp = "CONFIRMED|REJECTED", message = "Допустимые статусы: CONFIRMED, REJECTED")
    private String status;
}
