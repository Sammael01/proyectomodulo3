package co.edu.unbosque.tstyle.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class TtlCacheTest {

    private static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        public void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    @Test
    void shouldReturnValueBeforeItExpires() {
        MutableClock clock = new MutableClock();
        TtlCache<String, String> cache = new TtlCache<>(10, Duration.ofMinutes(5), clock);

        cache.put("a", "uno");
        clock.advance(Duration.ofMinutes(4));

        assertEquals("uno", cache.get("a").orElseThrow());
    }

    @Test
    void shouldDropValueAfterItExpires() {
        MutableClock clock = new MutableClock();
        TtlCache<String, String> cache = new TtlCache<>(10, Duration.ofMinutes(5), clock);

        cache.put("a", "uno");
        clock.advance(Duration.ofMinutes(5));

        assertTrue(cache.get("a").isEmpty());
    }

    @Test
    void shouldEvictOldestEntryWhenFull() {
        TtlCache<String, String> cache =
                new TtlCache<>(2, Duration.ofMinutes(5), new MutableClock());

        cache.put("a", "uno");
        cache.put("b", "dos");
        cache.put("c", "tres");

        assertTrue(cache.get("a").isEmpty(), "El más antiguo debe salir al llenarse");
        assertEquals("tres", cache.get("c").orElseThrow());
    }
}