package co.edu.unbosque.tstyle.cache;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Caché en memoria con vencimiento y tamaño máximo.
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 *
 * @author Sebastián
 */
public class TtlCache<K, V> {

    private record CachedValue<V>(V value, Instant expiresAt) {
    }

    private final Map<K, CachedValue<V>> entries;
    private final Duration ttl;
    private final Clock clock;

    /**
     * Crea la caché.
     *
     * @param maxEntries cantidad máxima de elementos
     * @param ttl tiempo de vida de cada elemento
     * @param clock reloj usado para calcular el vencimiento
     */
    public TtlCache(int maxEntries, Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CachedValue<V>> eldest) {
                return size() > maxEntries;
            }
        };
    }

    /**
     * Obtiene un valor vigente.
     *
     */
    public synchronized Optional<V> get(K key) {
        CachedValue<V> cached = entries.get(key);
        if (cached == null) {
            return Optional.empty();
        }
        if (!cached.expiresAt().isAfter(clock.instant())) {
            entries.remove(key);
            return Optional.empty();
        }
        return Optional.of(cached.value());
    }

    /**
     * Guarda un valor.
     *
     */
    public synchronized void put(K key, V value) {
        entries.put(key, new CachedValue<>(value, clock.instant().plus(ttl)));
    }
}