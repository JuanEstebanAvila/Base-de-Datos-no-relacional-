package com.gestorbd.motor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gestor de colecciones de la base de datos NoSQL.
 *
 * Se encarga de administrar multiples colecciones
 * donde cada coleccion es un GestorBaseDatos independiente con su propio
 * archivo JSON y su propio arbol AVL en memoria.
 *
 * Ciclo de vida:
 *   1. Al construirse, escanea la carpeta base y carga todas las colecciones
 *      existentes automaticamente.
 *   2. Cada coleccion creada genera un archivo .json en la carpeta base.
 *   3. Al borrar una coleccion, se elimina tanto de la RAM como del disco.
 *
 * Complejidad:
 *   - crearColeccion()   : O(1) en el mapa + O(k log k) para cargar k docs del archivo
 *   - borrarColeccion()  : O(1) en el mapa + O(1) para borrar el archivo
 *   - obtenerColeccion() : O(1) en el mapa
 *   - listarColecciones(): O(n) donde n es el numero de colecciones
 */
public class GestorColecciones {

    /**
     * Mapa que mantiene los gestores de cada coleccion cargados en RAM.
     * Clave: nombre de la coleccion. Valor: su GestorBaseDatos asociado.
     */
    private final Map<String, GestorBaseDatos> colecciones;

    /**
     * Ruta de la carpeta raiz donde se guardan todos los archivos .json
     * de las colecciones.
     */
    private final String carpetaBase;

    /**
     * Crea el gestor de colecciones apuntando a la carpeta indicada.
     * Si la carpeta no existe la crea automaticamente.
     * Si ya existen archivos .json en ella, los carga como colecciones.
     *
     * @param carpetaBase Ruta de la carpeta donde se almacenan las colecciones.
     */
    public GestorColecciones(String carpetaBase) {
        this.carpetaBase = carpetaBase;
        this.colecciones = new HashMap<>();

        // Crear la carpeta automaticamente si no existe para no crearla a mano
        // el que lo crea es mkdirs 
        File dir = new File(carpetaBase);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        cargarColeccionesExistentes();
    }

    /**
     * Escanea la carpeta base al arrancar y levanta un GestorBaseDatos
     * por cada archivo .json que encuentre.
     * Esto garantiza que las colecciones persisten entre reinicios del programa.
     * Complejidad: O(k log k) k es el numero de documentos en cada archivo.
     */
    private void cargarColeccionesExistentes() {
        File dir = new File(carpetaBase);

        // Filtrar solo los archivos con extension .json
        //listFiles()listar el contenido de una carpeta. (devuelve los archivos y subcarpetas de la condicion)
        // endsWith(".json")comprueba si un texto termina con una secuencia de caracteres determinada.
        File[] archivos = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (archivos != null) {
            for (File archivo : archivos) {
                // Quitar la extension .json para obtener el nombre de la coleccion
                String nombreColeccion = archivo.getName().replace(".json", "");
                //getAbsolutePath() devuelve la ruta completa de un archivo o directorio, 
                //incluyendo las carpetas raíz
                GestorBaseDatos gestor = new GestorBaseDatos(archivo.getAbsolutePath());
                colecciones.put(nombreColeccion, gestor);
            }
        }
    }

    /**
     * Crea una nueva coleccion con el nombre indicado.
     * Genera un archivo .json vacio en la carpeta base y lo registra en el mapa.
     * Si ya existe una coleccion con ese nombre, no hace nada y retorna false.
     * Complejidad: O(1) en el mapa.
     *
     * @param nombre Nombre de la nueva coleccion.
     * @return true si se creo exitosamente, false si ya existia.
     */
    public boolean crearColeccion(String nombre) {
        //Primero valida que no exista un duplicado en la RAM con containsKey.
        if (colecciones.containsKey(nombre)) {
            return false; // Ya existe una coleccion con ese nombre
        }

        // Construye la ruta 
        String rutaArchivo = new File(carpetaBase, nombre + ".json").getAbsolutePath();

        // Al instanciar GestorBaseDatos se crea el archivo y se inicia el arbol AVL
        GestorBaseDatos nuevoGestor = new GestorBaseDatos(rutaArchivo);
        colecciones.put(nombre, nuevoGestor);
        return true;
    }

    /**
     * Borra una coleccion existente.
     * Elimina el GestorBaseDatos de la RAM y borra el archivo .json del disco.
     * Si no existe la coleccion, no hace nada y retorna false.
     * Complejidad: O(1) en el mapa + O(1) para borrar el archivo.
     *
     * @param nombre Nombre de la coleccion a borrar.
     * @return true si se borro exitosamente, false si no existia.
     */
    public boolean borrarColeccion(String nombre) {
        if (!colecciones.containsKey(nombre)) {
            return false; // No existe la coleccion
        }

        // Eliminar de la RAM primero
        colecciones.remove(nombre);

        // Eliminar el archivo fisico del disco
        File archivo = new File(carpetaBase, nombre + ".json");
        if (archivo.exists()) {
            return archivo.delete();
        }
        return true;
    }

    /**
     * Devuelve una lista con los nombres de todas las colecciones existentes.
     * Complejidad: O(n) donde n es el numero de colecciones.
     *
     * @return Lista de nombres de colecciones.
     */
    public List<String> listarColecciones() {
        return new ArrayList<>(colecciones.keySet());
    }

    /**
     * Obtiene el GestorBaseDatos asociado a una coleccion por su nombre.
     * Usado para realizar operaciones CRUD sobre una coleccion especifica.
     * Complejidad: O(1) en el mapa.
     *
     * @param nombre Nombre de la coleccion a obtener.
     * @return Optional con el GestorBaseDatos si existe, vacio si no.
     */
    public Optional<GestorBaseDatos> obtenerColeccion(String nombre) {
        return Optional.ofNullable(colecciones.get(nombre));
    }
}