package com.gestorbd.arbol;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias para AVLArbol.
 * Verifica inserción, búsqueda, eliminación, balance y otros comportamientos.
 */
class AVLArbolTest {

    private AVLArbol<Integer, String> arbol;

    @BeforeEach
    public void inicializar() {
        arbol = new AVLArbol<>();
    }

    @Test
    void testInsertarYBuscar() {
        System.out.println("\n[InsertarYBuscar] Estado inicial: " + arbol.obtenerTodasLasClaves());
        arbol.insertar(10, "Diez");
        arbol.insertar(20, "Veinte");
        arbol.insertar(30, "Treinta");
        System.out.println("[InsertarYBuscar] Estado tras inserciones: " + arbol.obtenerTodasLasClaves());
        arbol.imprimirArbol();

        assertEquals(Optional.of("Diez"),    arbol.buscar(10));
        assertEquals(Optional.of("Veinte"),  arbol.buscar(20));
        assertEquals(Optional.of("Treinta"), arbol.buscar(30));
    }

    @Test
    void testInsertarDuplicadoActualizaValor() {
        arbol.insertar(10, "Valor Inicial");
        arbol.insertar(10, "Valor Actualizado");

        assertEquals(Optional.of("Valor Actualizado"), arbol.buscar(10));
        assertEquals(1, arbol.getTamanio());
    }

    @Test
    void testBuscarClaveInexistente() {
        arbol.insertar(10, "Diez");

        assertEquals(Optional.empty(), arbol.buscar(99));
    }

    @Test
    void testEliminar() {
        arbol.insertar(10, "Diez");
        arbol.insertar(20, "Veinte");
        arbol.insertar(30, "Treinta");
        System.out.println("\n[Eliminar] Antes de eliminar 20: " + arbol.obtenerTodasLasClaves());
        arbol.imprimirArbol();

        assertTrue(arbol.eliminar(20));
        System.out.println("[Eliminar] Después de eliminar 20: " + arbol.obtenerTodasLasClaves());
        arbol.imprimirArbol();

        assertEquals(Optional.empty(), arbol.buscar(20));
        assertEquals(2, arbol.getTamanio());
    }

    @Test
    void testEliminarClaveInexistente() {
        arbol.insertar(10, "Diez");

        assertFalse(arbol.eliminar(99));
        assertEquals(1, arbol.getTamanio());
    }

    @Test
    void testContiene() {
        arbol.insertar(10, "Diez");

        assertTrue(arbol.contiene(10));
        assertFalse(arbol.contiene(99));
    }

    @Test
    void testTamanio() {
        assertEquals(0, arbol.getTamanio());

        arbol.insertar(10, "Diez");
        assertEquals(1, arbol.getTamanio());

        arbol.insertar(20, "Veinte");
        arbol.insertar(30, "Treinta");
        assertEquals(3, arbol.getTamanio());

        arbol.eliminar(20);
        assertEquals(2, arbol.getTamanio());
    }

    @Test
    void testEstaVacio() {
        assertTrue(arbol.estaVacio());

        arbol.insertar(10, "Diez");
        assertFalse(arbol.estaVacio());

        arbol.eliminar(10);
        assertTrue(arbol.estaVacio());
    }

    @Test
    void testObtenerTodasLasClaves() {
        arbol.insertar(30, "Treinta");
        arbol.insertar(10, "Diez");
        arbol.insertar(20, "Veinte");
        arbol.insertar(40, "Cuarenta");

        List<Integer> claves = arbol.obtenerTodasLasClaves();

        assertEquals(4, claves.size());
        assertEquals(List.of(10, 20, 30, 40), claves);
    }

    @Test
    void testLimpiar() {
        arbol.insertar(10, "Diez");
        arbol.insertar(20, "Veinte");
        arbol.insertar(30, "Treinta");

        arbol.limpiar();

        assertTrue(arbol.estaVacio());
        assertEquals(0, arbol.getTamanio());
    }

    @Test
    void testBalanceTrasInserciones() {
        // Inserción que fuerza rotación Derecha-Derecha
        arbol.insertar(10, "Diez");
        arbol.insertar(20, "Veinte");
        arbol.insertar(30, "Treinta");
        System.out.println("\n[BalanceTrasInserciones] Estado: " + arbol.obtenerTodasLasClaves());
        arbol.imprimirArbol();

        assertTrue(arbol.contiene(10));
        assertTrue(arbol.contiene(20));
        assertTrue(arbol.contiene(30));
    }

    @Test
    void testBalanceTrasEliminaciones() {
        arbol.insertar(10, "Diez");
        arbol.insertar(20, "Veinte");
        arbol.insertar(30, "Treinta");
        arbol.insertar(40, "Cuarenta");
        arbol.insertar(50, "Cincuenta");
        System.out.println("\n[BalanceTrasEliminaciones] Antes: " + arbol.obtenerTodasLasClaves());
        arbol.imprimirArbol();

        arbol.eliminar(10);
        arbol.eliminar(20);
        System.out.println("[BalanceTrasEliminaciones] Después de eliminar 10 y 20: " + arbol.obtenerTodasLasClaves());
        arbol.imprimirArbol();

        verificarNingunNodoDesbalanceado(arbol);
        assertEquals(3, arbol.getTamanio());
    }

    /**
     * Verifica recursivamente que ningún nodo tenga factor de balance absoluto >= 2.
     */
    private void verificarNingunNodoDesbalanceado(AVLArbol<Integer, String> arbol) {
        verificarNodo(arbol.getRaiz());
    }

    private void verificarNodo(AVLNodo<Integer, String> nodo) {
        if (nodo == null) return;
        int balance = calcularBalance(nodo);
        assertTrue(Math.abs(balance) <= 1,
                "Nodo con clave " + nodo.getClave() + " tiene factor de balance = " + balance);
        verificarNodo(nodo.getIzquierdo());
        verificarNodo(nodo.getDerecho());
    }

    private int calcularBalance(AVLNodo<Integer, String> nodo) {
        int altIzq = (nodo.getIzquierdo() != null) ? nodo.getIzquierdo().getAltura() : 0;
        int altDer = (nodo.getDerecho()   != null) ? nodo.getDerecho().getAltura()   : 0;
        return altDer - altIzq;
    }

    @Test
    void testGranCantidadDeInserciones() {
        for (int i = 1; i <= 1000; i++) {
            arbol.insertar(i, "Valor" + i);
        }

        assertEquals(1000, arbol.getTamanio());
        assertEquals(Optional.of("Valor500"),  arbol.buscar(500));
        assertEquals(Optional.of("Valor1"),    arbol.buscar(1));
        assertEquals(Optional.of("Valor1000"), arbol.buscar(1000));
    }
}
