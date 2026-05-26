package com.gestorbd.motor;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gestorbd.modelo.Documento;

/**
 * Pruebas unitarias para GestorBaseDatos.
 * Verifica operaciones CRUD, persistencia, búsquedas y balance del árbol AVL.
 */
class GestorBaseDatosTest {

    private static final String ARCHIVO_PRUEBA = "test_db.json";
    private GestorBaseDatos gestor;
    private ObjectMapper mapeadorJson;

    @BeforeEach
    void inicializar() {
        File archivo = new File(ARCHIVO_PRUEBA);
        if (archivo.exists()) archivo.delete();
        gestor      = new GestorBaseDatos(ARCHIVO_PRUEBA);
        mapeadorJson = new ObjectMapper();
    }

    @AfterEach
    void limpiar() {
        File archivo = new File(ARCHIVO_PRUEBA);
        if (archivo.exists()) archivo.delete();
    }

    private Documento crearDocumento(Integer id, String nombre, int edad, String ciudad) {
        ObjectNode datos = mapeadorJson.createObjectNode();
        datos.put("nombre", nombre);
        datos.put("edad",   edad);
        datos.put("ciudad", ciudad);
        return new Documento(id, datos);
    }

    @Test
    void testGuardarYBuscarPorId() {
        System.out.println("\n[testGuardarYBuscarPorId]");
        Documento doc = crearDocumento(1, "Juan", 25, "Bogotá");

        gestor.guardar(doc);
        gestor.imprimirIndice();

        Optional<Documento> encontrado = gestor.buscarPorId(1);
        assertTrue(encontrado.isPresent());
        assertEquals("Juan", encontrado.get().getDatos().get("nombre").asText());
        assertEquals(25,     encontrado.get().getDatos().get("edad").asInt());
    }

    @Test
    void testGuardarVariosEImprimirArbol() {
        System.out.println("\n[testGuardarVariosEImprimirArbol]");
        gestor.guardar(crearDocumento(5, "Ana",    30, "Medellín"));
        gestor.guardar(crearDocumento(3, "Pedro",  22, "Cali"));
        gestor.guardar(crearDocumento(7, "María",  28, "Bogotá"));
        gestor.guardar(crearDocumento(1, "Luis",   35, "Barranquilla"));
        gestor.guardar(crearDocumento(9, "Carla",  27, "Cartagena"));

        System.out.println("Claves ordenadas: " + gestor.obtenerTodasLasClaves());
        gestor.imprimirIndice();

        assertEquals(5, gestor.obtenerTamanio());
    }

    @Test
    void testBuscarPorCampoExacto() {
        System.out.println("\n[testBuscarPorCampoExacto]");
        gestor.guardar(crearDocumento(1, "Juan",  25, "Bogotá"));
        gestor.guardar(crearDocumento(2, "Ana",   30, "Bogotá"));
        gestor.guardar(crearDocumento(3, "Pedro", 22, "Cali"));

        List<Documento> bogotanos = gestor.buscarPorCampoExacto("ciudad", "Bogotá");
        System.out.println("Documentos en Bogotá: " + bogotanos.size());

        assertEquals(2, bogotanos.size());
    }

    @Test
    void testBuscarPorCampoContiene() {
        System.out.println("\n[testBuscarPorCampoContiene]");
        gestor.guardar(crearDocumento(1, "Juan Carlos",  25, "Bogotá"));
        gestor.guardar(crearDocumento(2, "Ana María",    30, "Medellín"));
        gestor.guardar(crearDocumento(3, "Carlos Pedro", 22, "Cali"));

        List<Documento> conCarlos = gestor.buscarPorCampo("nombre", "Carlos");
        System.out.println("Documentos con 'Carlos' en nombre: " + conCarlos.size());

        assertEquals(2, conCarlos.size());
    }

    @Test
    void testActualizar() {
        System.out.println("\n[testActualizar]");
        gestor.guardar(crearDocumento(1, "Juan", 25, "Bogotá"));

        System.out.println("Antes de actualizar:");
        gestor.imprimirIndice();

        Documento actualizado = crearDocumento(1, "Juan Actualizado", 26, "Medellín");
        boolean resultado = gestor.actualizar(actualizado);

        System.out.println("Después de actualizar:");
        gestor.imprimirIndice();

        assertTrue(resultado);
        Optional<Documento> encontrado = gestor.buscarPorId(1);
        assertTrue(encontrado.isPresent());
        assertEquals("Juan Actualizado", encontrado.get().getDatos().get("nombre").asText());
        assertEquals(26, encontrado.get().getDatos().get("edad").asInt());
    }

    @Test
    void testActualizarInexistente() {
        System.out.println("\n[testActualizarInexistente]");
        Documento doc = crearDocumento(999, "Fantasma", 0, "Nada");

        boolean resultado = gestor.actualizar(doc);

        assertFalse(resultado);
    }

    @Test
    void testEliminar() {
        System.out.println("\n[testEliminar]");
        gestor.guardar(crearDocumento(1, "Juan",  25, "Bogotá"));
        gestor.guardar(crearDocumento(2, "Ana",   30, "Medellín"));
        gestor.guardar(crearDocumento(3, "Pedro", 22, "Cali"));

        System.out.println("Antes de eliminar:");
        gestor.imprimirIndice();

        boolean resultado = gestor.eliminarPorId(2);

        System.out.println("Después de eliminar '2':");
        gestor.imprimirIndice();

        assertTrue(resultado);
        assertEquals(2, gestor.obtenerTamanio());
        assertFalse(gestor.existePorId(2));
    }

    @Test
    void testEliminarInexistente() {
        System.out.println("\n[testEliminarInexistente]");
        gestor.guardar(crearDocumento(1, "Juan", 25, "Bogotá"));

        boolean resultado = gestor.eliminarPorId(999);

        assertFalse(resultado);
        assertEquals(1, gestor.obtenerTamanio());
    }

    @Test
    void testPersistencia() {
        System.out.println("\n[testPersistencia]");
        gestor.guardar(crearDocumento(1, "Juan", 25, "Bogotá"));
        gestor.guardar(crearDocumento(2, "Ana",  30, "Medellín"));

        // Crear nuevo gestor que carga desde el mismo archivo
        GestorBaseDatos gestor2 = new GestorBaseDatos(ARCHIVO_PRUEBA);

        System.out.println("Datos cargados desde archivo:");
        gestor2.imprimirIndice();

        assertEquals(2, gestor2.obtenerTamanio());
        assertTrue(gestor2.buscarPorId(1).isPresent());
        assertTrue(gestor2.buscarPorId(2).isPresent());
    }

    @Test
    void testLimpiar() {
        System.out.println("\n[testLimpiar]");
        gestor.guardar(crearDocumento(1, "Juan", 25, "Bogotá"));
        gestor.guardar(crearDocumento(2, "Ana",  30, "Medellín"));

        gestor.limpiar();

        assertTrue(gestor.estaVacia());
        assertEquals(0, gestor.obtenerTamanio());
    }

    @Test
    void testBuscarPorCondicion() {
        System.out.println("\n[testBuscarPorCondicion]");
        gestor.guardar(crearDocumento(1, "Juan",  25, "Bogotá"));
        gestor.guardar(crearDocumento(2, "Ana",   30, "Medellín"));
        gestor.guardar(crearDocumento(3, "Pedro", 35, "Cali"));
        gestor.guardar(crearDocumento(4, "María", 28, "Bogotá"));

        // Buscar mayores de 27 años
        List<Documento> mayores = gestor.buscarPorCondicion(doc ->
                doc.getDatos().get("edad").asInt() > 27);

        System.out.println("Mayores de 27 años: " + mayores.size());
        assertEquals(3, mayores.size());
    }

    @Test
    void testBalanceTrasOperaciones() {
        System.out.println("\n[testBalanceTrasOperaciones]");

        // Insertar en orden ascendente (peor caso para BST sin balance)
        for (int i = 1; i <= 10; i++) {
            gestor.guardar(crearDocumento(i, "Persona" + i, 20 + i, "Ciudad" + i));
        }

        System.out.println("Después de insertar 1-10 en orden:");
        gestor.imprimirIndice();

        gestor.eliminarPorId(5);
        gestor.eliminarPorId(3);
        gestor.eliminarPorId(8);

        System.out.println("Después de eliminar 5, 3, 8:");
        gestor.imprimirIndice();

        assertEquals(7, gestor.obtenerTamanio());
    }
}
