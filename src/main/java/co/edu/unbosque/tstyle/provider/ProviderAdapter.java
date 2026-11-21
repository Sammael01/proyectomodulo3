package co.edu.unbosque.tstyle.provider;

import java.util.List;

import co.edu.unbosque.tstyle.dto.ProductImportRequest;

/**
 * Fuente de productos de un proveedor. Cada integración autorizada
 * implementa esta interfaz y entrega registros ya normalizados, de modo
 * que el catálogo no dependa de un proveedor concreto.
 *
 * @author Sebastián
 */
public interface ProviderAdapter {

    /**
     * Obtiene los productos de la fuente.
     *
     * @return registros listos para validar e importar
     */
    List<ProductImportRequest> fetchProducts();
}
