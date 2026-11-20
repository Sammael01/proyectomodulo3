package co.edu.unbosque.tstyle.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Respuesta paginada con una estructura JSON estable.
 *
 * @param <T> tipo de los elementos
 * @param content elementos de la página actual
 * @param page número de página (empieza en 0)
 * @param size tamaño de página
 * @param totalElements total de elementos
 * @param totalPages total de páginas
 *
 * @author Equipo T Style
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /**
     * Convierte una página de Spring Data en una respuesta estable.
     *
     * @param page página original
     * @param <T> tipo de los elementos
     * @return respuesta paginada
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
