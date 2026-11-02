package ru.practicum.main.model;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {
    @Column(name = "lat", nullable = false)
    private float lat;
    @Column(name = "lon", nullable = false)
    private float lon;
}
