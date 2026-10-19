package com.tecsup;

import com.tecsup.aspect.*;
import com.tecsup.controller.ProductoController;
import com.tecsup.exception.GlobalExceptionHandler;
import com.tecsup.model.*;
import com.tecsup.repository.*;
import com.tecsup.service.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class Demo01ApplicationTests {
    private ProductoRepository productos;
    private AuditoriaRepository logs;
    private MockHttpServletRequest request;
    private MockMvc mvc;
    private Producto producto;
    private static final String JSON = "{\"nombre\":\"SSD\",\"precio\":150,\"stock\":3}";
    @BeforeEach
    void configurar() {
        productos = mock(ProductoRepository.class);
        logs = mock(AuditoriaRepository.class);
        request = new MockHttpServletRequest();
        producto = new Producto(5L, "SSD", 100, 2);
        when(productos.findById(5L)).thenReturn(Optional.of(producto));
        when(productos.findAll()).thenReturn(List.of(producto));
        when(productos.save(any())).thenAnswer(inv -> {
            Producto p = inv.getArgument(0);
            if (p.getId() == null) p.setId(6L);
            return p;
        });
        AuditoriaService auditoria = new AuditoriaService(logs);
        AuditoriaAspect aspecto = new AuditoriaAspect(auditoria, request);
        AspectJProxyFactory factory = new AspectJProxyFactory(new ProductoService(productos));
        factory.addAspect(new ErrorAspect(auditoria, aspecto));
        factory.addAspect(aspecto);
        ProductoService service = factory.getProxy();
        mvc = MockMvcBuilders.standaloneSetup(new ProductoController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    private void usuario(String nombre, String rol) {
        request.addHeader("Usuario", nombre);
        request.addHeader("Rol", rol);
    }
    private AuditoriaLog log() {
        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(logs).save(captor.capture());
        return captor.getValue();
    }
    @Test
    void sinEncabezadosRetorna401YAuditaError() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized());
        verifyNoInteractions(productos);
        assertEquals("ERROR", log().getAccion());
    }
    @Test
    void rolFalsoRetorna403() throws Exception {
        usuario("Ana", "ADMIN");
        mvc.perform(post("/api/productos").contentType("application/json").content(JSON))
                .andExpect(status().isForbidden());
        verifyNoInteractions(productos);
        assertEquals("ERROR", log().getAccion());
    }
    @Test
    void userPuedeCrearYAuditaIdReal() throws Exception {
        usuario("Ana", "USER");
        mvc.perform(post("/api/productos").contentType("application/json").content(JSON))
                .andExpect(status().isCreated());
        AuditoriaLog log = log();
        assertEquals("CREAR", log.getAccion());
        assertEquals("Ana (USER)", log.getUsuario());
        assertTrue(log.getDetalle().contains("ID: 6"));
    }
    @Test
    void listarAuditaCantidadInclusoCero() throws Exception {
        usuario("Luis", "USER");
        when(productos.findAll()).thenReturn(List.of());
        mvc.perform(get("/api/productos")).andExpect(status().isOk()).andExpect(content().json("[]"));
        assertEquals("LISTAR", log().getAccion());
        ArgumentCaptor<AuditoriaLog> captor = ArgumentCaptor.forClass(AuditoriaLog.class);
        verify(logs).save(captor.capture());
        assertEquals("Cantidad de productos: 0", captor.getValue().getDetalle());
    }
    @Test
    void userNoPuedeActualizarNiEliminar() throws Exception {
        usuario("Ana", "USER");
        mvc.perform(put("/api/productos/5").contentType("application/json").content(JSON))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/productos/5")).andExpect(status().isForbidden());
        verifyNoInteractions(productos);
        verify(logs, times(2)).save(any());
    }
    @Test
    void adminActualizaConAccionCorrecta() throws Exception {
        usuario("Ricardo", "ADMIN");
        mvc.perform(put("/api/productos/5").contentType("application/json").content(JSON))
                .andExpect(status().isOk());
        assertEquals(150, producto.getPrecio());
        AuditoriaLog log = log();
        assertEquals("ACTUALIZAR", log.getAccion());
        assertEquals("Se actualizó producto con ID: 5", log.getDetalle());
    }
    @Test
    void adminEliminaYAuditaId() throws Exception {
        usuario("Ricardo", "ADMIN");
        mvc.perform(delete("/api/productos/5")).andExpect(status().isOk());
        verify(productos).delete(producto);
        assertEquals("Se eliminó producto ID: 5", log().getDetalle());
    }
    @Test
    void productoInexistenteRetorna404YRegistraError() throws Exception {
        usuario("Ricardo", "ADMIN");
        mvc.perform(delete("/api/productos/99")).andExpect(status().isNotFound());
        verify(productos, never()).delete(any());
        assertEquals("ERROR", log().getAccion());
    }
    @Test
    void consultaIndividualTambienRequierePermisos() throws Exception {
        mvc.perform(get("/api/productos/5")).andExpect(status().isUnauthorized());
        verifyNoInteractions(productos);
    }
    @Test
    void datosInvalidosRetornan400() throws Exception {
        usuario("Ricardo", "ADMIN");
        mvc.perform(post("/api/productos").contentType("application/json")
                .content("{\"nombre\":\"\",\"precio\":-1,\"stock\":-1}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(productos);
    }
    @Test
    void falloAlGuardarErrorNoOcultaExcepcionOriginal() throws Exception {
        usuario("Ricardo", "ADMIN");
        when(logs.save(any())).thenThrow(new IllegalStateException("BD no disponible"));
        mvc.perform(get("/api/productos/99")).andExpect(status().isNotFound());
        verify(logs).save(any());
    }
}
