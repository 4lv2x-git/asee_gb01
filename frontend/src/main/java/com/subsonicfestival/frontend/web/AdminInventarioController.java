package com.subsonicfestival.frontend.web;

import com.subsonicfestival.frontend.web.InventarioClient.Producto;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Panel de administracion para consultar el catalogo y el stock del gestor de
 * inventario (entradas, pases y merchandising).
 */
@Controller
public class AdminInventarioController {

    private static final Set<String> TIPOS = Set.of("entrada", "pase", "merchandising");
    private static final Set<String> ESTADOS_STOCK = Set.of("disponible", "bajo", "agotado");
    private static final Collator COLADOR_ES = Collator.getInstance(Locale.forLanguageTag("es"));

    private final InventarioClient client;

    public AdminInventarioController(InventarioClient client) {
        this.client = client;
    }

    @GetMapping("/admin/inventario")
    public String panel(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "all") String tipo,
            @RequestParam(required = false, defaultValue = "all") String stock,
            Model model) {
        List<Producto> productos = List.of();
        try {
            productos = client.listar();
        } catch (ServicioExternoException ex) {
            model.addAttribute("error", ex.getMessage());
        }

        String tipoFiltro = TIPOS.contains(tipo) ? tipo : "all";
        String stockFiltro = ESTADOS_STOCK.contains(stock) ? stock : "all";
        List<Producto> filtrados = filtrar(productos, q, tipoFiltro, stockFiltro);

        model.addAttribute("productosFiltrados", filtrados);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("tipoFiltro", tipoFiltro);
        model.addAttribute("stockFiltro", stockFiltro);
        model.addAttribute(
                "filtrado",
                !(q == null || q.isBlank()) || !"all".equals(tipoFiltro) || !"all".equals(stockFiltro));
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("unidadesStock", productos.stream().mapToLong(AdminInventarioController::stock).sum());
        model.addAttribute("valorInventario", productos.stream()
                .mapToDouble(producto -> precio(producto) * stock(producto))
                .sum());
        model.addAttribute("stockBajo", productos.stream()
                .filter(producto -> stock(producto) > 0 && stock(producto) <= 10)
                .count());
        model.addAttribute("agotados", productos.stream()
                .filter(producto -> stock(producto) <= 0)
                .count());
        return "admin-inventario";
    }

    private static List<Producto> filtrar(
            List<Producto> productos, String q, String tipo, String stock) {
        String termino = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return productos.stream()
                .filter(producto -> "all".equals(tipo) || tipo.equalsIgnoreCase(producto.tipo()))
                .filter(producto -> coincideStock(producto, stock))
                .filter(producto -> coincideTexto(producto, termino))
                .sorted(Comparator.comparing(
                        producto -> producto.nombre() == null ? "" : producto.nombre(), COLADOR_ES))
                .toList();
    }

    private static boolean coincideStock(Producto producto, String filtro) {
        int unidades = stock(producto);
        return switch (filtro) {
            case "disponible" -> unidades > 0;
            case "bajo" -> unidades > 0 && unidades <= 10;
            case "agotado" -> unidades <= 0;
            default -> true;
        };
    }

    private static boolean coincideTexto(Producto producto, String termino) {
        if (termino.isEmpty()) {
            return true;
        }
        return Stream.of(producto.nombre(), producto.descripcion(), producto.id(), producto.tipo())
                .anyMatch(valor -> valor != null && valor.toLowerCase(Locale.ROOT).contains(termino));
    }

    private static int stock(Producto producto) {
        return producto.stockDisponible() == null ? 0 : Math.max(0, producto.stockDisponible());
    }

    private static double precio(Producto producto) {
        return producto.precio() == null ? 0 : Math.max(0, producto.precio());
    }
}
