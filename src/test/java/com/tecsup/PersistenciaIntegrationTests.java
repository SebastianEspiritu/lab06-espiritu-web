package com.tecsup;
import com.tecsup.model.Producto;
import com.tecsup.repository.*;
import com.tecsup.service.ProductoService;
import com.tecsup.exception.ForbiddenException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:lab;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false"
})
class PersistenciaIntegrationTests {
    @Autowired ProductoService service;
    @Autowired AuditoriaRepository auditoria;
    @Autowired ProductoRepository productos;
    @AfterEach void limpiarRequest() { RequestContextHolder.resetRequestAttributes(); }
    @Test void crudYErroresSePersistenConAspectosReales() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Usuario", "Ricardo");
        request.addHeader("Rol", "ADMIN");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Producto p = service.guardar(new Producto(null, "SSD", 100, 3));
        service.actualizar(p.getId(), new Producto(null, "SSD nuevo", 120, 4));
        assertEquals(1, service.listar().size());
        service.eliminar(p.getId());
        assertEquals(0, productos.count());
        request.removeHeader("Usuario"); request.removeHeader("Rol");
        request.addHeader("Usuario", "Ana"); request.addHeader("Rol", "USER");
        assertThrows(ForbiddenException.class, () -> service.eliminar(p.getId()));
        var logs = auditoria.findAll();
        assertEquals(5, logs.size());
        assertEquals(java.util.List.of("CREAR", "ACTUALIZAR", "LISTAR", "ELIMINAR", "ERROR"),
                logs.stream().map(l -> l.getAccion()).toList());
        assertEquals("Ricardo (ADMIN)", logs.get(0).getUsuario());
        assertEquals("Ana (USER)", logs.get(4).getUsuario());
        assertEquals("Cantidad de productos: 1", logs.get(2).getDetalle());
    }
}
