package co.edu.unbosque.tstyle.dto;

import java.util.List;

/**
 * 
 * @param content elementos de la pagina actual
 * @param page numero de pagina
 * @param size tamaño de pagina
 * @param totalElements total de elementos que cumplen el filtro
 * @param totalPages total de paginas
 * @param <T> tipo de los elementos
 * 
 * @author Sebastián
 */

public record PageResponse<T> (
		List<T> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {

}
