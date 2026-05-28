package com.gestorbd.motor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestorbd.modelo.Documento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class GestorBaseDatosTest {

    private static final String RUTA_TEST = "test_archivo_db.json";
    private GestorBaseDatos gestor;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        mapper = new ObjectMapper();
        // Inicializamos el motor apuntando al archivo temporal
        gestor = new GestorBaseDatos(RUTA_TEST);
    }

    @AfterEach
    void tearDown() {
        // Al terminar cada test, borramos el archivo JSON de prueba del disco
        File archivo = new File(RUTA_TEST);
        if (archivo.exists()) {
            archivo.delete();
        }
    }

    @Test
    void testGuardarYPersistir() throws Exception {
        String jsonTexto = "{\"nombre\":\"Juan\",\"edad\":25}";
        Documento doc = new Documento(1, mapper.readTree(jsonTexto));
        gestor.guardar(doc);

        // 1. Validar que se guardo en el AVL en RAM
        Optional<Documento> recuperado = gestor.buscarPorId(1);
        assertTrue(recuperado.isPresent());
        assertEquals("Juan", recuperado.get().getDatos().get("nombre").asText());

        // 2. Validar la persistencia real: creamos otro gestor apuntando al mismo archivo
        // Si Jackson hizo bien su trabajo, el nuevo gestor cargara el registro del disco
        GestorBaseDatos nuevoGestorInstancia = new GestorBaseDatos(RUTA_TEST);
        assertTrue(nuevoGestorInstancia.buscarPorId(1).isPresent(), "El dato debio guardarse en el disco");
    }

    @Test
    void testBuscarPorCampoExacto() throws Exception {
        gestor.guardar(new Documento(1, mapper.readTree("{\"rol\":\"admin\",\"user\":\"julian\"}")));
        gestor.guardar(new Documento(2, mapper.readTree("{\"rol\":\"user\",\"user\":\"pepe\"}")));

        List<Documento> admins = gestor.buscarPorCampoExacto("rol", "admin");

        assertEquals(1, admins.size());
        assertEquals("julian", admins.get(0).getDatos().get("user").asText());
    }
    
    @Test
    void testActualizar() throws Exception {
        gestor.guardar(new Documento(1, mapper.readTree("{\"nombre\":\"Juan\",\"edad\":25}")));
        gestor.actualizar(new Documento(1, mapper.readTree("{\"nombre\":\"Juan\",\"edad\":26}")));

        Optional<Documento> actualizado = gestor.buscarPorId(1);
        assertTrue(actualizado.isPresent());
        assertEquals(26, actualizado.get().getDatos().get("edad").asInt());
    }

    @Test
    void testEliminar() throws Exception {
        gestor.guardar(new Documento(1, mapper.readTree("{\"nombre\":\"Juan\",\"edad\":25}")));
        boolean resultado = gestor.eliminarPorId(1);

        assertTrue(resultado);
        assertFalse(gestor.existePorId(1));
    }
}