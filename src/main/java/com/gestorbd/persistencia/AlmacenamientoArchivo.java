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
public class AlmacenamientoArchivo implements RepositorioDocumentos {

    private final File archivo;
    private final ObjectMapper mapeadorJson;
    private List<Documento> documentos;

    /**
     * Crea una instancia apuntando al archivo de persistencia indicado.
     * Si el archivo ya existe, carga los documentos que contiene.
     * @param rutaArchivo Ruta del archivo JSON de persistencia.
     */
    public AlmacenamientoArchivo(String rutaArchivo) {
        this.archivo       = new File(rutaArchivo);
        this.mapeadorJson  = new ObjectMapper();
        this.mapeadorJson.enable(SerializationFeature.INDENT_OUTPUT);
        this.documentos    = new ArrayList<>();
        cargarDesdeArchivo();
    }

    /**
     * Lee los documentos del archivo si existe y tiene contenido.
     */
    private void cargarDesdeArchivo() {
        if (archivo.exists() && archivo.length() > 0) {
            try {
                documentos = mapeadorJson.readValue(archivo, new TypeReference<List<Documento>>() {});
            } catch (IOException e) {
                documentos = new ArrayList<>();
            }
        }
    }

    /**
     * Escribe la lista de documentos en el archivo.
     * Se llama automáticamente tras cada operación de escritura.
     */
    private void guardarEnArchivo() {
        try {
            mapeadorJson.writeValue(archivo, documentos);
        } catch (IOException e) {
            throw new RuntimeException("Error al persistir en archivo: " + e.getMessage(), e);
        }
    }

    @Override
    public void guardar(Documento documento) {
        Optional<Documento> existente = buscarPorId(documento.getId());
        if (existente.isPresent()) {
            int indice = documentos.indexOf(existente.get());
            documentos.set(indice, documento);
        } else {
            documentos.add(documento);
        }
        guardarEnArchivo();
    }

    @Override
    public Optional<Documento> buscarPorId(Integer id) {
        return documentos.stream()
                .filter(doc -> doc.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Documento> buscarTodos() {
        return new ArrayList<>(documentos);
    }

    @Override
    public List<Documento> buscarPorCondicion(Predicate<Documento> condicion) {
        List<Documento> resultado = new ArrayList<>();
        for (Documento doc : documentos) {
            if (condicion.test(doc)) {
                resultado.add(doc);
            }
        }
        return resultado;
    }

    @Override
    public List<Documento> buscarPorCampo(String nombreCampo, String valor) {
        return buscarPorCondicion(doc -> {
            JsonNode datos = doc.getDatos();
            if (datos == null) return false;
            JsonNode campo = datos.get(nombreCampo);
            if (campo == null) return false;
            return campo.asText().contains(valor);
        });
    }

    @Override
    public boolean eliminarPorId(Integer id) {
        Optional<Documento> existente = buscarPorId(id);
        if (existente.isPresent()) {
            documentos.remove(existente.get());
            guardarEnArchivo();
            return true;
        }
        return false;
    }

    @Override
    public boolean existePorId(Integer id) {
        return documentos.stream().anyMatch(doc -> doc.getId().equals(id));
    }

    @Override
    public boolean actualizar(Documento documento) {
        Optional<Documento> existente = buscarPorId(documento.getId());
        if (existente.isPresent()) {
            int indice = documentos.indexOf(existente.get());
            documentos.set(indice, documento);
            guardarEnArchivo();
            return true;
        }
        return false;
    }
}
