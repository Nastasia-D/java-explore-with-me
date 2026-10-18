package ru.practicum.ewm.stats.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "hit")
@Getter
@Setter
@ToString
public class Hit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app", nullable = false)
    private String app;

    @Column(name = "uri", nullable = false)
    private String uri;

    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "created", nullable = false)
    private LocalDateTime timestamp;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Hit)) return false;
        return id != null && id.equals(((Hit) o).getId());
    }

    @Override
    public int hashCode() {

        return getClass().hashCode();
    }
}
