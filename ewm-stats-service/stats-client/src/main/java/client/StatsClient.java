package client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StatsClient extends BaseClient {

    private final String serverUrl;

    public StatsClient(RestTemplate rest, String serverUrl) {
        super(rest);
        this.serverUrl = serverUrl;
    }

    public void hit(EndpointHit endpointHit) {
        post(serverUrl + "/hit", endpointHit);
    }

    public ResponseEntity<Object> stats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("end", end.format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", uris.toArray());
        }

        String url = builder.build().toUriString();
        return get(url);
    }
}
