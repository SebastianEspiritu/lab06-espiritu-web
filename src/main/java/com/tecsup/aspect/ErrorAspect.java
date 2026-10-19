package com.tecsup.aspect;
import com.tecsup.service.AuditoriaService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
@Aspect
@Component
@Order(0)
public class ErrorAspect {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorAspect.class);
    private final AuditoriaService auditoria;
    private final AuditoriaAspect acceso;
    public ErrorAspect(AuditoriaService auditoria, AuditoriaAspect acceso) {
        this.auditoria = auditoria;
        this.acceso = acceso;
    }
    // Se excluye AuditoriaService para evitar recursión si falla la persistencia del log.
    @AfterThrowing(pointcut = "execution(* com.tecsup.service.ProductoService.*(..))", throwing = "ex")
    public void capturarError(JoinPoint punto, Exception ex) {
        LOG.error("ERROR AOP en {}: {}", punto.getSignature().getName(), ex.getMessage());
        try {
            auditoria.registrar("ERROR", punto.getSignature().getName(),
                    ex.getClass().getSimpleName() + ": " + ex.getMessage(), acceso.usuarioActual());
        } catch (Exception falloAuditoria) {
            LOG.error("No se pudo guardar la auditoría del error", falloAuditoria);
        }
    }
}
