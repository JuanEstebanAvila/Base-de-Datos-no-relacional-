package com.gestorbd.arbol;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AVLarbolTest {

    private AVLArbol<Integer, String> arbol;

    @BeforeEach
    void setUp() {
        // Se ejecuta antes de cada test para darnos un arbol limpio
        arbol = new AVLArbol<>();
    }

    @Test
    void testInsertarYBuscar() {
        arbol.insertar(10, "Documento 10");
        arbol.insertar(20, "Documento 20");

        Optional<String> resultado = arbol.buscar(10);

        assertTrue(resultado.isPresent(), "El nodo 10 deberia existir");
        assertEquals("Documento 10", resultado.get());
    }

    @Test
    void testBuscarInexistente() {
        Optional<String> resultado = arbol.buscar(999);

        assertFalse(resultado.isPresent(), "No deberia encontrar una clave que no existe");
    }

    @Test
    void testBuscarPorRango() {
        arbol.insertar(10, "A");
        arbol.insertar(20, "B");
        arbol.insertar(30, "C");
        arbol.insertar(40, "D");

        // Prueba de la busqueda por rango del arbol AVL
        List<String> enRango = arbol.buscarPorRango(15, 35);

        assertEquals(2, enRango.size(), "Deberia encontrar 2 elementos (20 y 30)");
        assertTrue(enRango.contains("B"));
        assertTrue(enRango.contains("C"));
    }

    @Test
    void testEliminarNodo() {
        arbol.insertar(50, "Eliminame");

        assertTrue(arbol.contiene(50));

        boolean borrado = arbol.eliminar(50);

        assertTrue(borrado, "Deberia retornar true al borrar");
        assertFalse(arbol.contiene(50), "El nodo ya no deberia estar en el AVL");
    }
}