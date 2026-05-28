package com.gestorbd.interfazusuario;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestorbd.modelo.Documento;
import com.gestorbd.motor.GestorBaseDatos;
import com.gestorbd.motor.GestorColecciones;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * REPL (Read-Eval-Print Loop) de la base de datos NoSQL con arboles AVL.
 *
 * Proporciona una interfaz de linea de comandos interactiva que permite
 * al usuario gestionar colecciones y operar documentos JSON mediante
 * comandos simples.
 *
 * Todos los metodos de procesamiento retornan String para que tanto
 * el bucle de consola como la interfaz grafica puedan mostrar los resultados.
 *
 * Flujo de uso:
 *   1. Crear o seleccionar una coleccion con USE.
 *   2. Operar documentos con INSERTAR, BUSCAR, ACTUALIZAR, ELIMINAR.
 *   3. Salir con SALIR.
 *
 * Comandos disponibles:
 *   Globales : CREAR_COLECCION, BORRAR_COLECCION, LISTAR_COLECCIONES, USE
 *   Datos    : INSERTAR, BUSCAR, BUSCAR DONDE, BUSCAR_RANGO, ACTUALIZAR, ELIMINAR
 */
public class ReplBaseDatos {

    /** Gestor que administra todas las colecciones disponibles. */
    private final GestorColecciones gestorColecciones;

    /** Mapeador JSON para parsear los documentos ingresados por el usuario. */
    private final ObjectMapper mapper;

    /** Coleccion actualmente seleccionada con USE. Null si no hay ninguna. */
    private GestorBaseDatos coleccionActual;

    /** Nombre de la coleccion actualmente seleccionada. Vacio si no hay ninguna. */
    private String nombreColeccionActual;

    /**
     * Crea una instancia del REPL con el gestor de colecciones indicado.
     * @param gestorColecciones Gestor que administra las colecciones de la BD.
     */
    public ReplBaseDatos(GestorColecciones gestorColecciones) {
        this.gestorColecciones     = gestorColecciones;
        this.mapper                = new ObjectMapper();
        this.coleccionActual       = null;
        this.nombreColeccionActual = "";
    }

    /**
     * Inicia el ciclo REPL de consola: lee comandos del usuario,
     * los procesa y muestra el resultado hasta que el usuario escriba SALIR.
     * Complejidad: O(1) por iteracion, O(log n) a O(n) segun el comando.
     */
    public void iniciar() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("======================================================");
        System.out.println("   BIENVENIDO AL REPL DE TU BASE DE DATOS NOSQL AVL   ");
        System.out.println("======================================================");
        System.out.println("Comandos globales: Colecciones");
        System.out.println("  CREAR_COLECCION <nombre>");
        System.out.println("  BORRAR_COLECCION <nombre>");
        System.out.println("  LISTAR_COLECCIONES");
        System.out.println("  USE <nombre_coleccion>  (Para empezar a operar datos)");
        System.out.println("Comandos de datos (Requiere haber ejecutado USE primero):");
        System.out.println("  INSERTAR <id> <json>");
        System.out.println("  BUSCAR <id>");
        System.out.println("  BUSCAR DONDE <campo> = <valor>");
        System.out.println("  BUSCAR_RANGO <id_min> <id_max>");
        System.out.println("  ACTUALIZAR <id> <json>");
        System.out.println("  ELIMINAR <id>");
        System.out.println("  SALIR");
        System.out.println("======================================================\n");

        while (true) {
            // Prompt dinamico que muestra la coleccion activa
            String prompt = nombreColeccionActual.isEmpty()
                    ? "nosql> "
                    : "nosql(" + nombreColeccionActual + ")> ";
            System.out.print(prompt);

            String entrada = scanner.nextLine().trim();

            if (entrada.equalsIgnoreCase("SALIR")) {
                System.out.println("Cerrando el motor de base de datos. Hasta luego!");
                break;
            }

            if (entrada.isEmpty()) continue;

            // procesarComando ahora retorna el resultado como String
            String resultado = procesarComando(entrada);
            System.out.println(resultado);
        }

        scanner.close();
    }

    /**
     * Parsea y despacha el comando ingresado al metodo correspondiente
     * segun la palabra clave inicial. Retorna el resultado como String
     * para que tanto el REPL de consola como la GUI puedan mostrarlo.
     * Complejidad: O(1) para parsear + la del comando ejecutado.
     *
     * @param entrada Linea completa ingresada por el usuario.
     * @return Mensaje con el resultado de la operacion.
     */
    public String procesarComando(String entrada) {
        try {
            String[] partes   = entrada.split("\\s+", 2);
            String comando    = partes[0].toUpperCase();
            String argumentos = partes.length > 1 ? partes[1] : "";

            switch (comando) {

                // ──────────────────────────────────────────
                // GESTION DE COLECCIONES
                // ──────────────────────────────────────────

                case "CREAR_COLECCION":
                    if (argumentos.isEmpty())
                        return "ERROR: Falta el nombre de la coleccion.";
                    if (gestorColecciones.crearColeccion(argumentos)) {
                        return "OK: Coleccion '" + argumentos + "' creada con exito.";
                    }
                    return "AVISO: La coleccion '" + argumentos + "' ya existe.";

                case "BORRAR_COLECCION":
                    if (argumentos.isEmpty())
                        return "ERROR: Falta el nombre de la coleccion.";
                    if (gestorColecciones.borrarColeccion(argumentos)) {
                        // Si se borro la coleccion activa, resetear el contexto
                        if (argumentos.equals(nombreColeccionActual)) {
                            coleccionActual       = null;
                            nombreColeccionActual = "";
                        }
                        return "OK: Coleccion '" + argumentos + "' eliminada fisica y logicamente.";
                    }
                    return "ERROR: La coleccion '" + argumentos + "' no existe.";

                case "LISTAR_COLECCIONES":
                    List<String> colecciones = gestorColecciones.listarColecciones();
                    if (colecciones.isEmpty()) return "No hay colecciones creadas.";
                    return "Colecciones disponibles: " + colecciones;

                case "USE":
                    if (argumentos.isEmpty())
                        return "ERROR: Falta el nombre de la coleccion.";
                    Optional<GestorBaseDatos> cambio = gestorColecciones.obtenerColeccion(argumentos);
                    if (cambio.isPresent()) {
                        coleccionActual       = cambio.get();
                        nombreColeccionActual = argumentos;
                        return "OK: Contexto cambiado a la coleccion: " + nombreColeccionActual;
                    }
                    return "ERROR: La coleccion '" + argumentos + "' no existe. Creala primero con CREAR_COLECCION.";

                // ──────────────────────────────────────────
                // OPERACIONES CRUD
                // ──────────────────────────────────────────

                case "INSERTAR":
                    verificarContexto();
                    return ejecutarInsertarOActualizar(argumentos, true);

                case "ACTUALIZAR":
                    verificarContexto();
                    return ejecutarInsertarOActualizar(argumentos, false);

                case "BUSCAR":
                    verificarContexto();
                    if (argumentos.toUpperCase().startsWith("DONDE ")) {
                        return ejecutarBuscarDonde(argumentos.substring(6));
                    }
                    return ejecutarBuscarPorId(argumentos);

                case "ELIMINAR":
                    verificarContexto();
                    return ejecutarEliminar(argumentos);

                case "BUSCAR_RANGO":
                    verificarContexto();
                    return ejecutarBuscarRango(argumentos);

                default:
                    return "ERROR: Comando no reconocido: '" + comando + "'. " +
                           "Comandos validos: CREAR_COLECCION, USE, INSERTAR, BUSCAR, ACTUALIZAR, ELIMINAR, etc.";
            }
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Verifica que haya una coleccion activa seleccionada con USE.
     * Lanza excepcion si no hay ninguna seleccionada.
     * Complejidad: O(1)
     *
     * @throws IllegalStateException Si no hay coleccion activa.
     */
    private void verificarContexto() {
        if (coleccionActual == null) {
            throw new IllegalStateException(
                "Debes seleccionar una coleccion primero con el comando: USE <nombre>");
        }
    }

    /**
     * Ejecuta INSERTAR o ACTUALIZAR segun el flag recibido.
     * Parsea el ID y el bloque JSON del argumento y delega al gestor.
     * Formato esperado: <id> <json>  Ejemplo: 1 {"nombre":"Juan","edad":25}
     * Complejidad: O(log n)
     *
     * @param argumentos Cadena con el ID y el JSON separados por espacio.
     * @param esInsertar true para insertar, false para actualizar.
     * @return Mensaje con el resultado de la operacion.
     * @throws Exception Si el formato es incorrecto o el JSON es invalido.
     */
    private String ejecutarInsertarOActualizar(String argumentos, boolean esInsertar) throws Exception {
        String[] trozos = argumentos.split("\\s+", 2);
        if (trozos.length < 2)
            return "ERROR: Sintaxis incorrecta. Uso: COMANDO <id> <json>";

        Integer  id      = Integer.parseInt(trozos[0]);
        JsonNode jsonBody = mapper.readTree(trozos[1]);
        Documento doc    = new Documento(id, jsonBody);

        if (esInsertar) {
            coleccionActual.guardar(doc);
            return "OK: Documento [" + id + "] insertado en el AVL y persistido.";
        } else {
            if (coleccionActual.actualizar(doc)) {
                return "OK: Documento [" + id + "] actualizado con exito.";
            }
            return "ERROR: El ID [" + id + "] no existe en esta coleccion.";
        }
    }

    /**
     * Ejecuta BUSCAR por ID.
     * Formato esperado: <id>  Ejemplo: BUSCAR 1
     * Complejidad: O(log n)
     *
     * @param argumentos Cadena con el ID a buscar.
     * @return Mensaje con el documento encontrado o aviso de no encontrado.
     */
    private String ejecutarBuscarPorId(String argumentos) {
        if (argumentos.isEmpty())
            return "ERROR: Debes proveer un ID para buscar.";

        Integer id = Integer.parseInt(argumentos.trim());
        Optional<Documento> doc = coleccionActual.buscarPorId(id);

        if (doc.isPresent()) {
            return "ID " + id + " ->\n" + doc.get().getDatos().toPrettyString();
        }
        return "Documento con ID [" + id + "] no encontrado.";
    }

    /**
     * Ejecuta BUSCAR DONDE campo = valor.
     * Formato esperado: <campo> = <valor>  Ejemplo: nombre = "Juan"
     * Complejidad: O(n)
     *
     * @param argumentos Cadena con el campo, el operador = y el valor.
     * @return Mensaje con los documentos encontrados.
     */
    private String ejecutarBuscarDonde(String argumentos) {
        String[] partes = argumentos.split("=", 2);
        if (partes.length < 2)
            return "ERROR: Uso correcto: BUSCAR DONDE <campo> = <valor>";

        String campo = partes[0].trim();
        String valor = partes[1].trim().replace("\"", "");

        List<Documento> encontrados = coleccionActual.buscarPorCampoExacto(campo, valor);

        StringBuilder sb = new StringBuilder();
        sb.append("--- Resultados encontrados (").append(encontrados.size()).append(") ---\n");
        for (Documento d : encontrados) {
            sb.append("ID ").append(d.getId()).append(" -> ").append(d.getDatos()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Ejecuta BUSCAR_RANGO idMin idMax.
     * Formato esperado: <id_min> <id_max>  Ejemplo: BUSCAR_RANGO 1 10
     * Aprovecha la complejidad O(log n + m) del arbol AVL.
     *
     * @param argumentos Cadena con el ID minimo y maximo separados por espacio.
     * @return Mensaje con los documentos encontrados en el rango.
     */
    private String ejecutarBuscarRango(String argumentos) {
        String[] partes = argumentos.split("\\s+");
        if (partes.length < 2)
            return "ERROR: Uso correcto: BUSCAR_RANGO <id_min> <id_max>";

        Integer min = Integer.parseInt(partes[0]);
        Integer max = Integer.parseInt(partes[1]);

        List<Documento> encontrados = coleccionActual.buscarPorRangoId(min, max);

        StringBuilder sb = new StringBuilder();
        sb.append("--- Documentos en rango [").append(min).append(" - ").append(max)
          .append("] (ordenados por AVL) ---\n");
        for (Documento d : encontrados) {
            sb.append("ID ").append(d.getId()).append(" -> ").append(d.getDatos()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Ejecuta ELIMINAR por ID.
     * Formato esperado: <id>  Ejemplo: ELIMINAR 1
     * Complejidad: O(log n)
     *
     * @param argumentos Cadena con el ID a eliminar.
     * @return Mensaje con el resultado de la operacion.
     */
    private String ejecutarEliminar(String argumentos) {
        if (argumentos.isEmpty())
            return "ERROR: Debes proveer un ID para eliminar.";

        Integer id = Integer.parseInt(argumentos.trim());
        if (coleccionActual.eliminarPorId(id)) {
            return "OK: Documento [" + id + "] eliminado del AVL y del archivo JSON.";
        }
        return "ERROR: El ID [" + id + "] no existia en esta coleccion.";
    }

    /**
     * Devuelve el nombre de la coleccion actualmente seleccionada.
     * Usado por la GUI para mostrar el contexto activo.
     * Complejidad: O(1)
     *
     * @return Nombre de la coleccion activa o cadena vacia si no hay ninguna.
     */
    public String getNombreColeccionActual() {
        return nombreColeccionActual;
    }

    /**
     * Devuelve el GestorBaseDatos de la coleccion actualmente seleccionada.
     * Usado por la GUI para acceder a los documentos y al arbol AVL.
     * Complejidad: O(1)
     *
     * @return GestorBaseDatos activo o null si no hay coleccion seleccionada.
     */
    public GestorBaseDatos getColeccionActual() {
        return coleccionActual;
    }
}