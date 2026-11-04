package de.unchicks;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;

public final class Repos {
    private Repos() {}

    public interface ChickenRepo extends JpaRepository<Chicken, Long> {
        @Query("""
            select new de.unchicks.Api$ChickenDto(c.id, c.name, c.vendor, c.city, c.description,
                   coalesce(avg(k.rating), 0.0), count(k))
            from Chicken c left join CheckIn k on k.chicken = c
            group by c.id, c.name, c.vendor, c.city, c.description
            order by coalesce(avg(k.rating), 0.0) desc, count(k) desc
            """)
        List<Api.ChickenDto> findAllWithStats();
    }

    public interface CheckInRepo extends JpaRepository<CheckIn, Long> {
        @Query("""
            select new de.unchicks.Api$FeedDto(k.id, c.id, c.name, c.vendor, k.username, k.rating, k.comment, k.createdAt)
            from CheckIn k join k.chicken c order by k.createdAt desc
            """)
        List<Api.FeedDto> feed(Pageable page);

        @Query("""
            select new de.unchicks.Api$FeedDto(k.id, c.id, c.name, c.vendor, k.username, k.rating, k.comment, k.createdAt)
            from CheckIn k join k.chicken c where k.username = :user order by k.createdAt desc
            """)
        List<Api.FeedDto> feedOf(String user, Pageable page);
    }

    public interface UserRepo extends JpaRepository<AppUser, Long> {
        Optional<AppUser> findByUsername(String username);
    }
}
