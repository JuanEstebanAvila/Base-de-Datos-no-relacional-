package com.gestorbd.modelo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Modelo que representa un documento JSON almacenado en la base de datos.
 * Cada documento tiene un identificador unico numerico (id) y un contenido
 * en formato JSON (data). Es la unidad basica de almacenamiento del sistema.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documento {

    /**
     * Identificador único del documento (clave primaria numérica).
     * Usado como clave de indexacion en el arbol AVL.
     */
    private Integer id;

    /**
     * Contenido del documento en formato JSON.
     * Puede ser cualquier estructura JSON valida (objeto, arreglo, primitivo).
     */
    private JsonNode datos;
}
