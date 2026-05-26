package com.gestorbd.persistencia;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.gestorbd.modelo.Documento;

/**
 * Interfaz que define el contrato de persistencia para documentos JSON.
 * Cualquier implementación de almacenamiento (archivo, memoria, red, etc.)
 * debe implementar estas operaciones CRUD básicas.
 */
public interface RepositorioDocumentos {

    /**
     * Guarda un nuevo documento o reemplaza uno existente con el mismo id.
     * @param documento Documento a guardar.
     */
    void guardar(Documento documento);

    /**
     * Busca un documento por su identificador único.
     * @param id Identificador del documento.
     * @return Optional con el documento si existe, vacío si no.
     */
    Optional<Documento> buscarPorId(Integer id);

    /**
     * Devuelve todos los documentos almacenados.
     * @return Lista completa de documentos.
     */
    List<Documento> buscarTodos();

    /**
     * Busca documentos que cumplan una condición arbitraria.
     * @param condicion Función que evalúa si un documento cumple el criterio.
     * @return Lista de documentos que pasan la condición.
     */
    List<Documento> buscarPorCondicion(Predicate<Documento> condicion);

    /**
     * Busca documentos donde un campo específico contenga el valor dado.
     * @param nombreCampo Nombre del campo JSON a evaluar.
     * @param valor Texto que debe contener el campo.
     * @return Lista de documentos coincidentes.
     */
    List<Documento> buscarPorCampo(String nombreCampo, String valor);

    /**
     * Elimina un documento por su identificador.
     * @param id Identificador del documento a eliminar.
     * @return true si se eliminó, false si no existía.
     */
    boolean eliminarPorId(Integer id);

    /**
     * Verifica si existe un documento con el identificador dado.
     * @param id Identificador a verificar.
     * @return true si existe, false si no.
     */
    boolean existePorId(Integer id);

    /**
     * Actualiza un documento existente con nuevos datos.
     * @param documento Documento con los datos actualizados (debe tener id existente).
     * @return true si se actualizó, false si el id no existía.
     */
    boolean actualizar(Documento documento);
}
