package com.webempresarial.store.repository;

import com.webempresarial.store.dto.producto.publico.ProductoCardDTO;
import com.webempresarial.store.model.Producto;
import com.webempresarial.store.model.Store;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByProductNameAndStore(
            String productName,
            Store store
    );

    List<Producto> findByCategoriaIdAndStore(
            Long categoriaId,
            Store store
    );

    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.categoria.nombre = :categoria
        AND p.store = :store
    """)
    List<Producto> findByCategoriaNombre(
            @Param("categoria") String categoria,
            @Param("store") Store store
    );

    @EntityGraph(attributePaths = {"categoria"})
    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.id = :id
        AND p.store = :store
    """)
    Optional<Producto> findByIdWithCategoria(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Query("""
        SELECT new com.webempresarial.store.dto.producto.publico.ProductoCardDTO(
            p.id,
            p.productName,
            p.price,

            COALESCE(
                (SELECT MIN(v.precio)
                 FROM ProductoVariante v
                 WHERE v.producto.id = p.id),
                p.price
            ),

            CASE WHEN SIZE(p.variantes) > 0 THEN true ELSE false END,

            p.tienePromocion,
            p.porcentajeDescuento,

            COALESCE(
                (SELECT i.imageUrl
                 FROM ImagenProducto i
                 WHERE i.producto.id = p.id
                 ORDER BY i.principal DESC, i.orden ASC
                 LIMIT 1),
                '/img/default.png'
            ),

            c.nombre,
            m.nombre,
            p.stockSimple
        )
        FROM Producto p
        LEFT JOIN p.categoria c
        LEFT JOIN p.marca m
        WHERE p.visibleEnMenu = true
        AND p.store = :store
        ORDER BY p.id DESC
    """)
    List<ProductoCardDTO> findProductosIndexOptimizado(
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT p.categoria.nombre
        FROM Producto p
        WHERE p.visibleEnMenu = true
        AND p.store = :store
        AND p.categoria IS NOT NULL
        ORDER BY p.categoria.nombre
    """)
    List<String> obtenerNombresCategoriasVisibles(
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.marca
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.imagenes
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos
        WHERE p.visibleEnMenu = true
        AND p.store = :store
    """)
    List<Producto> findProductosVisiblesConTodo(
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.marca
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.imagenes
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos
        WHERE p.id = :id
        AND p.store = :store
    """)
    Optional<Producto> findByIdConTodo(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.marca
        LEFT JOIN FETCH p.categoria
        LEFT JOIN FETCH p.imagenes
        WHERE p.productName = :name
        AND p.store = :store
    """)
    Optional<Producto> findByProductNameConTodo(
            @Param("name") String name,
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos
        WHERE p.id = :id
        AND p.store = :store
    """)
    Optional<Producto> findByIdWithVariantes(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p
        FROM Producto p
        WHERE p.id = :id
        AND p.store = :store
    """)
    Optional<Producto> findByIdForUpdate(
            @Param("id") Long id,
            @Param("store") Store store
    );

    @Modifying
    @Query("""
        UPDATE Producto p
        SET p.visibleEnMenu = :visible
        WHERE p.categoria.id = :categoriaId
        AND p.store = :store
    """)
    void updateVisibilidadPorCategoria(
            @Param("categoriaId") Long categoriaId,
            @Param("visible") boolean visible,
            @Param("store") Store store
    );

    @Query(value = """
        SELECT
            p.id,
            p.product_name,
            p.price,

            COALESCE(
                (
                    SELECT MIN(v.precio)
                    FROM producto_variantes v
                    WHERE v.producto_id = p.id
                ),
                p.price
            ) AS precio_minimo,

            CASE
                WHEN EXISTS (
                    SELECT 1
                    FROM producto_variantes v
                    WHERE v.producto_id = p.id
                )
                THEN true
                ELSE false
            END AS tiene_variantes,

            p.tiene_promocion,
            c.nombre AS categoria_nombre

        FROM productos p
        LEFT JOIN categorias c ON c.id = p.categoria_id
        WHERE p.visible_en_menu = 1
        AND p.store_id = :storeId
    """, nativeQuery = true)
    List<Object[]> findProductosPrecioRaw(
            @Param("storeId") Long storeId
    );

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.categoria c
        LEFT JOIN FETCH p.marca m
        LEFT JOIN FETCH p.imagenes i
        WHERE p.visibleEnMenu = true
        AND p.store = :store
    """)
    List<Producto> findProductosAdminBase(
            @Param("store") Store store
    );

    @Query("""
        SELECT DISTINCT p
        FROM Producto p
        LEFT JOIN FETCH p.variantes v
        LEFT JOIN FETCH v.atributos a
        WHERE p.id IN :ids
        AND p.store = :store
    """)
    List<Producto> findProductosConVariantes(
            @Param("ids") List<Long> ids,
            @Param("store") Store store
    );
}