package com.gestorbd.persistencia;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gestorbd.modelo.Documento;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Implementación de RepositorioDocumentos que persiste los documentos
 * en un archivo JSON en disco. Mantiene una lista en memoria sincronizada
 * con el archivo cada vez que hay cambios.
 */
public class AlmacenamientoArchivo implements RepositorioDocumentos{

    private final File archivo;
    private final ObjectMapper mapeadorJson;

    /**
     * Crea una instancia apuntando al archivo de persistencia indicado.
     * Si el archivo ya existe, carga los documentos que contiene.
     * @param rutaArchivo Ruta del archivo JSON de persistencia.
     */
    public AlmacenamientoArchivo(String rutaArchivo) {
        this.archivo       = new File(rutaArchivo);
        this.mapeadorJson  = new ObjectMapper();
        this.mapeadorJson.enable(SerializationFeature.INDENT_OUTPUT);   //aplicara tabulaciones y saltos de linea
    }

    /**
     * Lee todos los documentos del archivo JSON.
     * Devuelve lista vacia si el archivo no existe o esta vacio.
     */
    public List<Documento> cargar() {
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return mapeadorJson.readValue(
                archivo, new TypeReference<List<Documento>>() {});
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Escribe la lista completa de documentos en el archivo JSON.
     */
    public void guardar(List<Documento> documentos) {
        try {
            mapeadorJson.writeValue(archivo, documentos);
        } catch (IOException e) {
            throw new RuntimeException(
                "Error al guardar en archivo: " + e.getMessage(), e);
        }
    }
}