package co.edu.unbosque.tstyle.dto;

import java.util.List;

/**
 * Resumen de una importación del catálogo.
 *
 * @param created cantidad de productos creados
 * @param updated cantidad de productos actualizados
 * @param failed  cantidad de registros rechazados
 * @param errors  detalle de los registros rechazados
 *
 * @author Sebastián
 */
public record CatalogImportResult(int created, int updated, int failed, List<String> errors) {
}
