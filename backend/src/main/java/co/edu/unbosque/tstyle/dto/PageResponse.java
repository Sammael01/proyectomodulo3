
package co.edu.unbosque.tstyle.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Representa una respuesta paginada del backend.
 *
 * @param content elementos de la página actual
 * @param page número de página
 * @param size tamaño de página
 * @param totalElements total de elementos
 * @param totalPages total de páginas
 * @param <T> tipo de los elementos
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
     * Convierte una página de Spring Data en una respuesta del proyecto.
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
