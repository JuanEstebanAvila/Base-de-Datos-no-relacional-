package com.gestorbd.arbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Implementación de un árbol AVL autobalanceado genérico.
 * Garantiza que la diferencia de alturas entre subárboles de cualquier nodo
 * nunca supere 1, lo que asegura operaciones en O(log n).
 *
 * Soporta inserción, búsqueda, eliminación y recorrido en orden.
 *
 * @param <C> Tipo de la clave (debe ser comparable)
 * @param <V> Tipo del valor almacenado en cada nodo
 */
@Data
@NoArgsConstructor
public class AVLArbol<C extends Comparable<C>, V> {

    /** Nodo raíz del árbol. */
    private AVLNodo<C, V> raiz;

    /** Número total de nodos en el árbol. */
    private int tamanio = 0;

    /**
     * Devuelve la altura de un nodo, o 0 si es nulo.
     * @param nodo El nodo a consultar
     * @return Altura del nodo
     */
    private int obtenerAltura(AVLNodo<C, V> nodo) {
        return nodo == null ? 0 : nodo.getAltura();
    }

    /**
     * Calcula el factor de balance de un nodo:
     * positivo si el subárbol izquierdo es más alto,
     * negativo si el derecho lo es.
     * @param nodo El nodo a evaluar
     * @return Factor de balance
     */
    private int calcularBalance(AVLNodo<C, V> nodo) {
        return nodo == null ? 0 : obtenerAltura(nodo.getIzquierdo()) - obtenerAltura(nodo.getDerecho());
    }

    /**
     * Actualiza la altura de un nodo en función de la altura de sus hijos.
     * @param nodo El nodo a actualizar
     */
    private void actualizarAltura(AVLNodo<C, V> nodo) {
        if (nodo != null) {
            nodo.setAltura(1 + Math.max(obtenerAltura(nodo.getIzquierdo()), obtenerAltura(nodo.getDerecho())));
        }
    }

    /**
     * Rotación simple a la derecha.
     * Se usa cuando el subárbol izquierdo está sobrecargado (caso II).
     * @param y Nodo desbalanceado
     * @return Nueva raíz tras la rotación
     */
    private AVLNodo<C, V> rotarDerecha(AVLNodo<C, V> y) {
        AVLNodo<C, V> x  = y.getIzquierdo();
        AVLNodo<C, V> t2 = x.getDerecho();

        x.setDerecho(y);
        y.setIzquierdo(t2);

        actualizarAltura(y);
        actualizarAltura(x);

        return x;
    }

    /**
     * Rotación simple a la izquierda.
     * Se usa cuando el subárbol derecho está sobrecargado (caso DD).
     * @param x Nodo desbalanceado
     * @return Nueva raíz tras la rotación
     */
    private AVLNodo<C, V> rotarIzquierda(AVLNodo<C, V> x) {
        AVLNodo<C, V> y  = x.getDerecho();
        AVLNodo<C, V> t2 = y.getIzquierdo();

        y.setIzquierdo(x);
        x.setDerecho(t2);

        actualizarAltura(x);
        actualizarAltura(y);

        return y;
    }

    /**
     * Inserta un par clave-valor en el árbol.
     * Si la clave ya existe, actualiza su valor.
     * @param clave Clave a insertar
     * @param valor Valor asociado
     */
    public void insertar(C clave, V valor) {
        raiz = insertarNodo(raiz, clave, valor);
    }

    /**
     * Inserción recursiva con rebalanceo tras cada llamada.
     */
    private AVLNodo<C, V> insertarNodo(AVLNodo<C, V> nodo, C clave, V valor) {
        if (nodo == null) {
            tamanio++;
            return new AVLNodo<>(clave, valor);
        }

        int comparacion = clave.compareTo(nodo.getClave());

        if (comparacion < 0) {
            nodo.setIzquierdo(insertarNodo(nodo.getIzquierdo(), clave, valor));
        } else if (comparacion > 0) {
            nodo.setDerecho(insertarNodo(nodo.getDerecho(), clave, valor));
        } else {
            // Clave duplicada: solo actualiza el valor
            nodo.setValor(valor);
            return nodo;
        }

        actualizarAltura(nodo);
        int balance = calcularBalance(nodo);

        // Caso Izquierda-Izquierda
        if (balance > 1 && clave.compareTo(nodo.getIzquierdo().getClave()) < 0) {
            return rotarDerecha(nodo);
        }
        // Caso Derecha-Derecha
        if (balance < -1 && clave.compareTo(nodo.getDerecho().getClave()) > 0) {
            return rotarIzquierda(nodo);
        }
        // Caso Izquierda-Derecha
        if (balance > 1 && clave.compareTo(nodo.getIzquierdo().getClave()) > 0) {
            nodo.setIzquierdo(rotarIzquierda(nodo.getIzquierdo()));
            return rotarDerecha(nodo);
        }
        // Caso Derecha-Izquierda
        if (balance < -1 && clave.compareTo(nodo.getDerecho().getClave()) < 0) {
            nodo.setDerecho(rotarDerecha(nodo.getDerecho()));
            return rotarIzquierda(nodo);
        }

        return nodo;
    }

    /**
     * Busca un valor por su clave.
     * @param clave Clave a buscar
     * @return Optional con el valor si existe, vacío si no
     */
    public Optional<V> buscar(C clave) {
        AVLNodo<C, V> nodo = buscarNodo(raiz, clave);
        return nodo == null ? Optional.empty() : Optional.of(nodo.getValor());
    }

    /**
     * Búsqueda recursiva de un nodo por clave.
     */
    private AVLNodo<C, V> buscarNodo(AVLNodo<C, V> nodo, C clave) {
        if (nodo == null) return null;

        int comparacion = clave.compareTo(nodo.getClave());
        if (comparacion < 0) return buscarNodo(nodo.getIzquierdo(), clave);
        if (comparacion > 0) return buscarNodo(nodo.getDerecho(), clave);
        return nodo;
    }

    /**
     * Elimina un nodo por su clave y rebalancea el árbol.
     * @param clave Clave a eliminar
     * @return true si se eliminó, false si no existía
     */
    public boolean eliminar(C clave) {
        if (!contiene(clave)) return false;
        raiz = eliminarNodo(raiz, clave);
        tamanio--;
        return true;
    }

    /**
     * Eliminación recursiva con rebalanceo tras cada llamada.
     */
    private AVLNodo<C, V> eliminarNodo(AVLNodo<C, V> nodo, C clave) {
        if (nodo == null) return null;

        int comparacion = clave.compareTo(nodo.getClave());

        if (comparacion < 0) {
            nodo.setIzquierdo(eliminarNodo(nodo.getIzquierdo(), clave));
        } else if (comparacion > 0) {
            nodo.setDerecho(eliminarNodo(nodo.getDerecho(), clave));
        } else {
            // Nodo encontrado
            if (nodo.getIzquierdo() == null || nodo.getDerecho() == null) {
                AVLNodo<C, V> temp = nodo.getIzquierdo() != null ? nodo.getIzquierdo() : nodo.getDerecho();
                if (temp == null) return null;    // Sin hijos
                return temp;                      // Un solo hijo
            } else {
                // Dos hijos: reemplazar con el sucesor inorden (mínimo del subárbol derecho)
                AVLNodo<C, V> sucesor = obtenerNodoMinimo(nodo.getDerecho());
                nodo.setClave(sucesor.getClave());
                nodo.setValor(sucesor.getValor());
                nodo.setDerecho(eliminarNodo(nodo.getDerecho(), sucesor.getClave()));
            }
        }

        actualizarAltura(nodo);
        int balance = calcularBalance(nodo);

        // Caso Izquierda-Izquierda
        if (balance > 1 && calcularBalance(nodo.getIzquierdo()) >= 0)
            return rotarDerecha(nodo);
        // Caso Izquierda-Derecha
        if (balance > 1 && calcularBalance(nodo.getIzquierdo()) < 0) {
            nodo.setIzquierdo(rotarIzquierda(nodo.getIzquierdo()));
            return rotarDerecha(nodo);
        }
        // Caso Derecha-Derecha
        if (balance < -1 && calcularBalance(nodo.getDerecho()) <= 0)
            return rotarIzquierda(nodo);
        // Caso Derecha-Izquierda
        if (balance < -1 && calcularBalance(nodo.getDerecho()) > 0) {
            nodo.setDerecho(rotarDerecha(nodo.getDerecho()));
            return rotarIzquierda(nodo);
        }

        return nodo;
    }

    /**
     * Devuelve el nodo con la clave mínima de un subárbol.
     * @param nodo Raíz del subárbol
     * @return Nodo con clave mínima
     */
    private AVLNodo<C, V> obtenerNodoMinimo(AVLNodo<C, V> nodo) {
        AVLNodo<C, V> actual = nodo;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }


    /**
     * Verifica si una clave existe en el árbol.
     * @param clave Clave a verificar
     * @return true si existe, false si no
     */
    public boolean contiene(C clave) {
        return buscarNodo(raiz, clave) != null;
    }

    /**
     * Devuelve todas las claves del árbol ordenadas (recorrido inorden).
     * @return Lista de claves en orden ascendente
     */
    public List<C> obtenerTodasLasClaves() {
        List<C> claves = new ArrayList<>();
        recorridoInorden(raiz, claves);
        return claves;
    }

    /**
     * Recorrido inorden recursivo para acumular claves.
     */
    private void recorridoInorden(AVLNodo<C, V> nodo, List<C> claves) {
        if (nodo != null) {
            recorridoInorden(nodo.getIzquierdo(), claves);
            claves.add(nodo.getClave());
            recorridoInorden(nodo.getDerecho(), claves);
        }
    }

    /**
     * Imprime el árbol como un array binario (índices estilo heap).
     * Útil para depurar la estructura interna del árbol.
     */
    public void imprimirArbol() {
        List<String> array = new ArrayList<>();
        llenarArray(raiz, 0, array);
        System.out.println("[AVLArbol] Representación tipo array binario:");
        for (int i = 0; i < array.size(); i++) {
            System.out.printf("[%d]: %s%n", i, array.get(i));
        }
    }

    /**
     * Rellena el array con nodos según índices de heap binario.
     */
    private void llenarArray(AVLNodo<C, V> nodo, int indice, List<String> array) {
        if (nodo == null) return;
        while (array.size() <= indice) array.add("null");
        array.set(indice, String.format("(%s,h=%d)", nodo.getClave(), nodo.getAltura()));
        llenarArray(nodo.getIzquierdo(), 2 * indice + 1, array);
        llenarArray(nodo.getDerecho(),   2 * indice + 2, array);
    }

    /**
     * Verifica si el árbol está vacío.
     * @return true si no hay nodos, false si hay al menos uno
     */
    public boolean estaVacio() {
        return raiz == null;
    }

    /**
     * Elimina todos los nodos del árbol y reinicia el tamaño.
     */
    public void limpiar() {
        raiz = null;
        tamanio = 0;
    }
}
