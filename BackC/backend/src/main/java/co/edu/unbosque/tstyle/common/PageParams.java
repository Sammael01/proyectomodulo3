package co.edu.unbosque.tstyle.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Construye paginaciones seguras a partir de los parámetros de la URL.
 *
 * @author Equipo T Style
 */
public final class PageParams {

    /** Tamaño máximo de página permitido. */
    public static final int MAX_SIZE = 100;

    private PageParams() {
    }

    /**
     * Crea una paginación ordenada por fecha de creación, del más reciente al más antiguo.
     * Corrige valores fuera de rango en lugar de fallar.
     *
     * @param page número de página (mínimo 0)
     * @param size tamaño de página (entre 1 y {@value #MAX_SIZE})
     * @return paginación segura
     */
    public static Pageable newestFirst(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
