package com.tecsup.aspect;
import com.tecsup.exception.ForbiddenException;
import com.tecsup.exception.UnauthorizedException;
import com.tecsup.model.Producto;
import com.tecsup.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
@Aspect
@Component
@Order(1)
public class AuditoriaAspect {
    private static final Map<String, String> USUARIOS = Map.of("Ricardo", "ADMIN", "Ana", "USER", "Luis", "USER");
    private final AuditoriaService auditoria;
    private final HttpServletRequest request;
    public AuditoriaAspect(AuditoriaService auditoria, HttpServletRequest request) {
        this.auditoria = auditoria;
        this.request = request;
    }
    public String usuarioActual() {
        String usuario = request.getHeader("Usuario");
        if (usuario == null || usuario.isBlank()) return "ANONIMO";
        String rol = USUARIOS.get(usuario);
        return rol == null ? "DESCONOCIDO" : usuario + " (" + rol + ")";
    }
    @Around("execution(* com.tecsup.service.ProductoService.*(..))")
    public Object controlarYAuditar(ProceedingJoinPoint punto) throws Throwable {
        String metodo = punto.getSignature().getName();
        validar(metodo);
        Object resultado = punto.proceed();
        String accion;
        String detalle;
        switch (metodo) {
            case "guardar" -> {
                Producto p = (Producto) resultado;
                accion = "CREAR";
                detalle = "Se creó producto ID: " + p.getId() + ", nombre=" + p.getNombre() + ", precio=" + p.getPrecio();
            }
            case "actualizar" -> {
                accion = "ACTUALIZAR";
                detalle = "Se actualizó producto con ID: " + punto.getArgs()[0];
            }
            case "eliminar" -> {
                accion = "ELIMINAR";
                detalle = "Se eliminó producto ID: " + punto.getArgs()[0];
            }
            case "listar" -> {
                accion = "LISTAR";
                detalle = "Cantidad de productos: " + ((List<?>) resultado).size();
            }
            case "obtener" -> {
                accion = "CONSULTAR";
                detalle = "Se consultó producto ID: " + punto.getArgs()[0];
            }
            default -> { return resultado; }
        }
        auditoria.registrar(accion, metodo, detalle, usuarioActual());
        return resultado;
    }
    private void validar(String metodo) {
        String usuario = request.getHeader("Usuario");
        String rol = request.getHeader("Rol");
        if (usuario == null || usuario.isBlank() || rol == null || rol.isBlank())
            throw new UnauthorizedException("Debe enviar Usuario y Rol");
        if (!rol.equals(USUARIOS.get(usuario))) throw new ForbiddenException("No tiene permisos");
        if (!rol.equals("ADMIN") && !List.of("guardar", "listar", "obtener").contains(metodo))
            throw new ForbiddenException("Acceso denegado");
    }
}
