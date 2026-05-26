package com.gestorbd.arbol;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Representa un nodo dentro del árbol AVL autobalanceado.
 * Cada nodo almacena una clave comparable, un valor asociado,
 * referencias a sus hijos izquierdo y derecho, y su altura actual.
 *
 * @param <C> Tipo de la clave (debe ser comparable)
 * @param <V> Tipo del valor almacenado en el nodo
 */
@Data
@RequiredArgsConstructor
public class AVLNodo<C extends Comparable<C>, V> {

    /** Hijo izquierdo del nodo (claves menores). */
    private AVLNodo<C, V> izquierdo;

    /** Hijo derecho del nodo (claves mayores). */
    private AVLNodo<C, V> derecho;

    /** Clave del nodo, usada para ordenar el árbol. */
    @NonNull
    private C clave;

    /** Valor asociado a la clave. */
    @NonNull
    private V valor;

    /** Altura del nodo dentro del árbol (1 para hojas). */
    private int altura = 1;
}
