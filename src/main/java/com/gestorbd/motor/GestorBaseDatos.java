package com.gestorbd.motor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.gestorbd.arbol.AVLArbol;
import com.gestorbd.modelo.Documento;

/**
 * Motor principal de la base de datos NoSQL.
 *
 * Actúa como fachada entre la interfaz de usuario y las capas internas.
 * Indexa todos los documentos en un árbol AVL para búsquedas O(log n)
 * y persiste los cambios en un archivo JSON en disco.
 *
 * Ciclo de vida:
 *   1. Al construirse, carga los documentos del archivo al árbol AVL.
 *   2. Cada operación de escritura actualiza el árbol y el archivo.
 *   3. El árbol también está disponible para que la interfaz lo dibuje.
 */
public class GestorBaseDatos {

    private final File archivo;
    private final ObjectMapper mapeadorJson;
    private final AVLArbol<Integer, Documento> indice;

    /**
     * Crea el gestor apuntando al archivo de persistencia indicado.
     * Si el archivo ya existe, carga y reindexca todos los documentos.
     * @param rutaArchivo Ruta del archivo JSON donde se almacenan los datos.
     */
    public GestorBaseDatos(String rutaArchivo) {
        this.archivo      = new File(rutaArchivo);
        this.mapeadorJson = new ObjectMapper();
        this.mapeadorJson.enable(SerializationFeature.INDENT_OUTPUT);
        this.indice       = new AVLArbol<>();
        cargarDesdeArchivo();
    }

    // ─────────────────────────────────────────────
    // Lectura / escritura en disco
    // ─────────────────────────────────────────────

    /**
     * Lee todos los documentos del archivo JSON y los inserta en el árbol AVL.
     * Si el archivo no existe o está vacío, el árbol queda vacío.
     */
    private void cargarDesdeArchivo() {
        if (archivo.exists() && archivo.length() > 0) {
            try {
                List<Documento> documentos = mapeadorJson.readValue(
                        archivo, new TypeReference<List<Documento>>() {});
                for (Documento doc : documentos) {
                    indice.insertar(doc.getId(), doc);
                }
            } catch (IOException e) {
                // El árbol queda vacío si el archivo está corrupto
            }
        }
    }

    /**
     * Serializa todos los documentos del árbol al archivo JSON.
     * Se llama automáticamente tras cada operación de escritura.
     */
    private void guardarEnArchivo() {
        try {
            List<Documento> documentos = obtenerTodosLosDocumentos();
            mapeadorJson.writeValue(archivo, documentos);
        } catch (IOException e) {
            throw new RuntimeException("Error al persistir en archivo: " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────
    // Operaciones CRUD
    // ─────────────────────────────────────────────

    /**
     * Inserta un documento nuevo o reemplaza uno existente con el mismo id.
     * @param documento Documento a guardar (no puede ser nulo ni tener id nulo).
     */
    public void guardar(Documento documento) {
        if (documento == null || documento.getId() == null) {
            throw new IllegalArgumentException("El documento y su ID no pueden ser nulos");
        }
        indice.insertar(documento.getId(), documento);
        guardarEnArchivo();
    }

    /**
     * Busca un documento por su id usando el árbol AVL (O(log n)).
     * @param id Identificador del documento.
     * @return Optional con el documento si existe, vacío si no.
     */
    public Optional<Documento> buscarPorId(Integer id) {
        return indice.buscar(id);
    }

    /**
     * Busca documentos que cumplan una condición arbitraria.
     * Recorre todos los documentos, por lo que es O(n).
     * @param condicion Función que evalúa si un documento cumple el criterio.
     * @return Lista de documentos que pasan la condición.
     */
    public List<Documento> buscarPorCondicion(Predicate<Documento> condicion) {
        List<Documento> resultado = new ArrayList<>();
        for (Documento doc : obtenerTodosLosDocumentos()) {
            if (condicion.test(doc)) resultado.add(doc);
        }
        return resultado;
    }

    /**
     * Busca documentos donde un campo JSON contenga el texto dado.
     * @param nombreCampo Nombre del campo JSON a evaluar.
     * @param valor Texto que debe contener el campo.
     * @return Lista de documentos coincidentes.
     */
    public List<Documento> buscarPorCampo(String nombreCampo, String valor) {
        return buscarPorCondicion(doc -> {
            JsonNode datos = doc.getDatos();
            if (datos == null) return false;
            JsonNode campo = datos.get(nombreCampo);
            if (campo == null) return false;
            return campo.asText().contains(valor);
        });
    }

    /**
     * Busca documentos donde un campo JSON sea exactamente igual al valor dado.
     * @param nombreCampo Nombre del campo JSON a evaluar.
     * @param valor Valor exacto que debe tener el campo.
     * @return Lista de documentos coincidentes.
     */
    public List<Documento> buscarPorCampoExacto(String nombreCampo, String valor) {
        return buscarPorCondicion(doc -> {
            JsonNode datos = doc.getDatos();
            if (datos == null) return false;
            JsonNode campo = datos.get(nombreCampo);
            if (campo == null) return false;
            return campo.asText().equals(valor);
        });
    }

    /**
     * Actualiza un documento existente. No crea uno nuevo si no existe.
     * @param documento Documento con los nuevos datos (debe tener id existente).
     * @return true si se actualizó, false si el id no existe.
     */
    public boolean actualizar(Documento documento) {
        if (documento == null || documento.getId() == null) {
            throw new IllegalArgumentException("El documento y su ID no pueden ser nulos");
        }
        if (!indice.contiene(documento.getId())) return false;
        indice.insertar(documento.getId(), documento);
        guardarEnArchivo();
        return true;
    }

    /**
     * Elimina un documento por su id del árbol y del archivo.
     * @param id Identificador del documento a eliminar.
     * @return true si se eliminó, false si no existía.
     */
    public boolean eliminarPorId(Integer id) {
        if (!indice.eliminar(id)) return false;
        guardarEnArchivo();
        return true;
    }

    // ─────────────────────────────────────────────
    // Consultas de estado
    // ─────────────────────────────────────────────

    /**
     * Verifica si existe un documento con el id dado.
     * @param id Identificador a verificar.
     * @return true si existe, false si no.
     */
    public boolean existePorId(Integer id) {
        return indice.contiene(id);
    }

    /**
     * Devuelve todos los documentos ordenados por id (recorrido inorden del AVL).
     * @return Lista de todos los documentos en orden ascendente de id.
     */
    public List<Documento> obtenerTodosLosDocumentos() {
        List<Documento> documentos = new ArrayList<>();
        for (Integer clave : indice.obtenerTodasLasClaves()) {
            indice.buscar(clave).ifPresent(documentos::add);
        }
        return documentos;
    }

    /**
     * Devuelve el número total de documentos almacenados.
     * @return Cantidad de documentos.
     */
    public int obtenerTamanio() {
        return indice.getTamanio();
    }

    /**
     * Indica si no hay ningún documento almacenado.
     * @return true si la base de datos está vacía, false si tiene al menos uno.
     */
    public boolean estaVacia() {
        return indice.estaVacio();
    }

    /**
     * Elimina todos los documentos del árbol y del archivo.
     */
    public void limpiar() {
        indice.limpiar();
        guardarEnArchivo();
    }

    /**
     * Imprime la estructura interna del árbol AVL en la consola.
     * Útil para depuración.
     */
    public void imprimirIndice() {
        indice.imprimirArbol();
    }

    /**
     * Devuelve todos los ids ordenados ascendentemente.
     * @return Lista de ids en orden.
     */
    public List<Integer> obtenerTodasLasClaves() {
        return indice.obtenerTodasLasClaves();
    }

    /**
     * Devuelve el árbol AVL interno.
     * Usado por la interfaz gráfica para visualizar la estructura.
     * @return Árbol AVL con los documentos indexados.
     */
    public AVLArbol<Integer, Documento> obtenerIndice() {
        return indice;
    }
}
