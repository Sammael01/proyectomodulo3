#!/usr/bin/env python3
"""
Genera catalogo.json para el importador del backend T Style usando DummyJSON
(https://dummyjson.com), una API publica gratuita con DATOS DE DEMOSTRACION.

Uso (Python 3.8+, solo libreria estandar, necesita internet):
    python generar_catalogo.py
Resultado: catalogo.json en la misma carpeta. Cópialo a
src/main/resources/catalog/catalogo.json y configura:
    catalog.import.path=classpath:catalog/catalogo.json
    catalog.import.enabled=true   (una sola vez; luego vuelve a false)

Los productos son de relleno: no son de una tienda real y externalUrl apunta
a la API de DummyJSON, no a una pagina de compra. Documentalo en el PR.
"""
import json
import sys
import urllib.request

BASE = "https://dummyjson.com/products/category/{slug}?limit=100"
PROVEEDOR = "DummyJSON"
MONEDA = "USD"  # el precio se guarda tal cual lo informa la fuente, sin convertir

# categoria de la API -> categoria del catalogo
CATEGORIAS = {
    "tops": "Camisetas y tops",
    "womens-dresses": "Vestidos",
    "mens-shirts": "Camisas hombre",
    "womens-shoes": "Calzado mujer",
    "mens-shoes": "Calzado hombre",
    "womens-bags": "Bolsos",
    "sunglasses": "Gafas de sol",
}


def recortar(texto, maximo):
    texto = (texto or "").strip()
    return texto[:maximo]


def convertir(productos, categoria):
    """Convierte productos de DummyJSON al formato del importador.
    Omite los que no tengan los datos obligatorios."""
    resultado = []
    for p in productos:
        identificador = p.get("id")
        nombre = recortar(p.get("title"), 200)
        imagen = recortar(p.get("thumbnail"), 500)
        precio = p.get("price")
        if identificador is None or not nombre or not imagen or precio is None or precio < 0:
            continue

        estado = p.get("availabilityStatus")
        if estado is not None:
            disponible = estado.strip().lower() != "out of stock"
        elif p.get("stock") is not None:
            disponible = p["stock"] > 0
        else:
            disponible = None  # la fuente no lo informa

        resultado.append({
            "provider": PROVEEDOR,
            "externalId": recortar(str(identificador), 100),
            "name": nombre,
            "description": recortar(p.get("description"), 2000) or None,
            "imageUrl": imagen,
            "price": precio,
            "currency": MONEDA,
            "externalUrl": f"https://dummyjson.com/products/{identificador}",
            "category": categoria,
            "providerAvailable": disponible,
        })
    return resultado


def descargar(slug):
    with urllib.request.urlopen(BASE.format(slug=slug), timeout=15) as respuesta:
        return json.load(respuesta).get("products", [])


def main():
    catalogo = []
    for slug, categoria in CATEGORIAS.items():
        try:
            productos = descargar(slug)
        except Exception as error:
            print(f"No se pudo leer '{slug}': {error}", file=sys.stderr)
            continue
        convertidos = convertir(productos, categoria)
        print(f"{categoria}: {len(convertidos)} productos")
        catalogo.extend(convertidos)

    if not catalogo:
        print("No se generó ningún producto; revisa tu conexión.", file=sys.stderr)
        sys.exit(1)

    with open("catalogo.json", "w", encoding="utf-8") as archivo:
        json.dump(catalogo, archivo, ensure_ascii=False, indent=2)
    print(f"Listo: catalogo.json con {len(catalogo)} productos.")


if __name__ == "__main__":
    main()
