package de.unchicks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class Api {
    public record ChickenDto(Long id, String name, String vendor, String city, String description, double avgRating, long checkIns) {}
    public record FeedDto(Long id, Long chickenId, String chickenName, String vendor, String username, int rating, String comment, Instant createdAt) {}
    public record NewChicken(@NotBlank @Size(max = 120) String name, @Size(max = 120) String vendor,
                             @Size(max = 120) String city, @Size(max = 1000) String description) {}
    public record NewCheckIn(@Min(1) @Max(5) int rating, @Size(max = 1000) String comment) {}

    private final Repos.ChickenRepo chickens;
    private final Repos.CheckInRepo checkIns;

    public Api(Repos.ChickenRepo chickens, Repos.CheckInRepo checkIns) { this.chickens = chickens; this.checkIns = checkIns; }

    @GetMapping("/chickens")
    List<ChickenDto> chickens() { return chickens.findAllWithStats(); }

    @PostMapping("/chickens") @ResponseStatus(HttpStatus.CREATED)
    Long addChicken(@Valid @RequestBody NewChicken in) {
        return chickens.save(new Chicken(in.name().trim(), in.vendor(), in.city(), in.description())).id;
    }

    /** Globaler Feed (öffentlich). */
    @GetMapping("/checkins")
    List<FeedDto> feed() { return checkIns.feed(PageRequest.of(0, 50)); }

    /** Eigener Feed (nur eingeloggt). */
    @GetMapping("/checkins/mine")
    List<FeedDto> mine(Authentication auth) { return checkIns.feedOf(auth.getName(), PageRequest.of(0, 50)); }

    @PostMapping("/chickens/{id}/checkins") @ResponseStatus(HttpStatus.CREATED)
    Long checkIn(@PathVariable Long id, @Valid @RequestBody NewCheckIn in, Authentication auth) {
        Chicken c = chickens.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hähnchen nicht gefunden"));
        return checkIns.save(new CheckIn(c, auth.getName(), in.rating(), in.comment())).id;
    }
}
