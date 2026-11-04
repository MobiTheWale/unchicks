package de.unchicks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class Auth {
    public record Credentials(@NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "[A-Za-z0-9_.-]+") String username,
                              @NotBlank @Size(min = 8, max = 100) String password) {}
    public record Session(String token, String username) {}

    private final Repos.UserRepo users;
    private final PasswordEncoder encoder;
    private final JwtEncoder jwt;

    public Auth(Repos.UserRepo users, PasswordEncoder encoder, JwtEncoder jwt) { this.users = users; this.encoder = encoder; this.jwt = jwt; }

    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    Session register(@Valid @RequestBody Credentials in) {
        String name = in.username().trim();
        if (users.findByUsername(name).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Name bereits vergeben");
        users.save(new AppUser(name, encoder.encode(in.password())));
        return session(name);
    }

    @PostMapping("/login")
    Session login(@Valid @RequestBody Credentials in) {
        AppUser u = users.findByUsername(in.username().trim())
            .filter(x -> encoder.matches(in.password(), x.passwordHash))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Name oder Passwort falsch"));
        return session(u.username);
    }

    private Session session(String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(username).issuedAt(now).expiresAt(now.plus(7, ChronoUnit.DAYS)).build();
        String token = jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new Session(token, username);
    }
}
