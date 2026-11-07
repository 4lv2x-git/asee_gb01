package com.subsonicfestival.inventory.config;

import com.subsonicfestival.inventory.entity.Producto;
import com.subsonicfestival.inventory.entity.TipoProducto;
import com.subsonicfestival.inventory.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductoRepository productoRepository;

    public DataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            log.info("Inicializando datos de catálogo de inventario...");

            List<Producto> productosIniciales = List.of(
                    Producto.builder()
                            .id("prod-001")
                            .nombre("Entrada General - Subsonic Festival Day 1")
                            .descripcion("Acceso general a todos los escenarios principales durante el día 1.")
                            .precio(45.00)
                            .tipo(TipoProducto.ENTRADA)
                            .stockDisponible(250)
                            .build(),
                    Producto.builder()
                            .id("prod-002")
                            .nombre("Entrada General - Subsonic Festival Day 2")
                            .descripcion("Acceso general a todos los escenarios principales durante el día 2.")
                            .precio(45.00)
                            .tipo(TipoProducto.ENTRADA)
                            .stockDisponible(200)
                            .build(),
                    Producto.builder()
                            .id("pase-001")
                            .nombre("Pase VIP Completo (3 Días)")
                            .descripcion("Pase VIP con acceso a zona preferente, barra libre y backstage.")
                            .precio(120.00)
                            .tipo(TipoProducto.PASE)
                            .stockDisponible(50)
                            .build(),
                    Producto.builder()
                            .id("pase-002")
                            .nombre("Pase Camping + Festival (3 Días)")
                            .descripcion("Acceso completo al festival con parcela de camping reservada.")
                            .precio(85.00)
                            .tipo(TipoProducto.PASE)
                            .stockDisponible(80)
                            .build(),
                    Producto.builder()
                            .id("merch-001")
                            .nombre("Camiseta Oficial Subsonic Festival 2026")
                            .descripcion("Camiseta 100% algodón orgánico edición limitada.")
                            .precio(25.00)
                            .tipo(TipoProducto.MERCHANDISING)
                            .stockDisponible(150)
                            .build(),
                    Producto.builder()
                            .id("merch-002")
                            .nombre("Sudadera con Capucha Subsonic Sound")
                            .descripcion("Sudadera negra bordada con el logo del festival.")
                            .precio(45.00)
                            .tipo(TipoProducto.MERCHANDISING)
                            .stockDisponible(60)
                            .build()
            );

            productoRepository.saveAll(productosIniciales);
            log.info("Catálogo de inventario inicializado con {} productos.", productosIniciales.size());
        }
    }
}
