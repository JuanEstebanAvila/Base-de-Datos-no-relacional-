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
 * REPL de la base de datos con arboles AVL.
 *
 * Proporciona una interfaz de linea de comandos que permite
 * al usuario gestionar colecciones y operar documentos JSON mediante
 * comandos simples.
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
        this.gestorColecciones    = gestorColecciones;
        this.mapper               = new ObjectMapper();
        this.coleccionActual      = null;
        this.nombreColeccionActual = "";
    }

    /**
     * Inicia el ciclo REPL: muestra el menu, lee comandos del usuario,
     * los procesa y muestra el resultado hasta que el usuario escriba SALIR.
     */
    public void iniciar() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("   BIENVENIDO AL REPL DE TU BASE DE DATOS NOSQL AVL   ");
        System.out.println("Comandos globales: Colecciones");
        System.out.println("  CREAR_COLECCION <nombre>");
        System.out.println("  BORRAR_COLECCION <nombre>");
        System.out.println("  LISTAR_COLECCIONES");
        System.out.println("  USE <nombre_coleccion>  (Para empezar a operar datos)");
        System.out.println("Comandos de datos (Requiere haber ejecutado USE primero):");
        System.out.println("  INSERTAR id {json} ");
        System.out.println("  BUSCAR <id>");
        System.out.println("  BUSCAR DONDE <campo> = <valor>");
        System.out.println("  BUSCAR_RANGO <id_min> <id_max>");
        System.out.println("  ACTUALIZAR <id> <json>");
        System.out.println("  ELIMINAR <id>");
        System.out.println("  SALIR");

        while (true) {
            // Prompt dinamico que muestra la coleccion activa
            String prompt = nombreColeccionActual.isEmpty()
                    ? "nosql> "
                    : "nosql(" + nombreColeccionActual + ")> ";
            System.out.print(prompt);

            String entrada = scanner.nextLine().trim();

            if (entrada.equalsIgnoreCase("SALIR")) {
                System.out.println("Cerrando el motor de base de datos.");
                break;
            }

            if (entrada.isEmpty()) continue;

            try {
                procesarComando(entrada);
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }

        scanner.close();
    }

    /**
     * Parsea y despacha el comando ingresado por el usuario al metodo
     * correspondiente segun la palabra clave inicial.
     *
     * @param entrada Linea completa ingresada por el usuario.
     * @throws Exception Si el comando tiene sintaxis incorrecta o falla la operacion.
     */
    private void procesarComando(String entrada) throws Exception {
        String[] partes  = entrada.split("\\s+", 2);
        String comando   = partes[0].toUpperCase();
        String argumentos = partes.length > 1 ? partes[1] : "";

        switch (comando) {

            case "CREAR_COLECCION":
                if (argumentos.isEmpty())
                    throw new IllegalArgumentException("Falta el nombre de la coleccion.");
                if (gestorColecciones.crearColeccion(argumentos)) {
                    System.out.println("Coleccion '" + argumentos + "' se creo.");
                } else {
                    System.out.println("La coleccion '" + argumentos + "' ya existe.");
                }
                break;

            case "BORRAR_COLECCION":
                if (argumentos.isEmpty())
                    throw new IllegalArgumentException("Falta el nombre de la coleccion.");
                if (gestorColecciones.borrarColeccion(argumentos)) {
                    System.out.println("Coleccion '" + argumentos + "' eliminada. ");
                    // Si se borro la coleccion activa, resetear el contexto
                    if (argumentos.equals(nombreColeccionActual)) {
                        coleccionActual       = null;
                        nombreColeccionActual = "";
                    }
                } else {
                    System.out.println("La coleccion '" + argumentos + "' no existe.");
                }
                break;

            case "LISTAR_COLECCIONES":
                List<String> colecciones = gestorColecciones.listarColecciones();
                if (colecciones.isEmpty()) {
                    System.out.println("No hay colecciones creadas.");
                } else {
                    System.out.println("Colecciones disponibles: " + colecciones);
                }
                break;

            case "USE":
                if (argumentos.isEmpty())
                    throw new IllegalArgumentException("Falta el nombre de la coleccion.");
                Optional<GestorBaseDatos> cambio = gestorColecciones.obtenerColeccion(argumentos);
                if (cambio.isPresent()) {
                    coleccionActual       = cambio.get();
                    nombreColeccionActual = argumentos;
                    System.out.println("Contexto cambiado a la coleccion: " + nombreColeccionActual);
                } else {
                    System.out.println("La coleccion '" + argumentos + "' no existe. crearla primero con CREAR_COLECCION.");
                }
                break;

            case "INSERTAR":
                verificarContexto();
                ejecutarInsertarOActualizar(argumentos, true);
                break;

            case "ACTUALIZAR":
                verificarContexto();
                ejecutarInsertarOActualizar(argumentos, false);
                break;

            case "BUSCAR":
                verificarContexto();
                // Detecta si es BUSCAR DONDE o BUSCAR por ID
                if (argumentos.toUpperCase().startsWith("DONDE ")) {
                    ejecutarBuscarDonde(argumentos.substring(6));
                } else {
                    ejecutarBuscarPorId(argumentos);
                }
                break;

            case "ELIMINAR":
                verificarContexto();
                ejecutarEliminar(argumentos);
                break;

            case "BUSCAR_RANGO":
                verificarContexto();
                ejecutarBuscarRango(argumentos);
                break;

            default:
                System.out.println("Comando no reconocido: '" + comando + "'.");
                System.out.println("Escribe uno de los comandos validos: CREAR_COLECCION, USE, INSERTAR, BUSCAR, etc.");
        }
    }

    /**
     * Verifica que haya una coleccion activa seleccionada con USE.
     * Lanza excepcion si no hay ninguna seleccionada.
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
     *
     * @param argumentos Cadena con el ID y el JSON separados por espacio.
     * @param esInsertar true para insertar, false para actualizar.
     * @throws Exception Si el formato es incorrecto o el JSON es invalido.
     */
    private void ejecutarInsertarOActualizar(String argumentos, boolean esInsertar) throws Exception {
        String[] trozos = argumentos.split("\\s+", 2);
        if (trozos.length < 2)
            throw new IllegalArgumentException("Sintaxis incorrecta. Uso: COMANDO <id> <json>");

        Integer  id      = Integer.parseInt(trozos[0]);
        JsonNode jsonBody = mapper.readTree(trozos[1]);
        Documento doc    = new Documento(id, jsonBody);

        if (esInsertar) {
            coleccionActual.guardar(doc);
            System.out.println("OK: Documento [" + id + "] insertado en el AVL y persistido.");
        } else {
            if (coleccionActual.actualizar(doc)) {
                System.out.println("OK: Documento [" + id + "] actualizado con exito.");
            } else {
                System.out.println("ERROR: El ID [" + id + "] no existe en esta coleccion.");
            }
        }
    }

    /**
     * Ejecuta BUSCAR por ID.
     * Formato esperado: <id>  Ejemplo: BUSCAR 1
     *
     * @param argumentos Cadena con el ID a buscar.
     */
    private void ejecutarBuscarPorId(String argumentos) {
        if (argumentos.isEmpty())
            throw new IllegalArgumentException("Debes proveer un ID para buscar.");

        Integer id = Integer.parseInt(argumentos.trim());
        Optional<Documento> doc = coleccionActual.buscarPorId(id);

        if (doc.isPresent()) {
            System.out.println("ID " + id + " ->");
            System.out.println(doc.get().getDatos().toPrettyString());
        } else {
            System.out.println("Documento con ID [" + id + "] no encontrado.");
        }
    }

    /**
     * Ejecuta BUSCAR DONDE campo = valor.
     * Formato esperado: <campo> = <valor>  Ejemplo: nombre = "Juan"
     *
     * @param argumentos Cadena con el campo, el operador = y el valor.
     */
    private void ejecutarBuscarDonde(String argumentos) {
        String[] partes = argumentos.split("=", 2);
        if (partes.length < 2)
            throw new IllegalArgumentException("Uso correcto: BUSCAR DONDE <campo> = <valor>");

        String campo = partes[0].trim();
        // Quitar comillas si el usuario las incluyo
        String valor = partes[1].trim().replace("\"", "");

        List<Documento> encontrados = coleccionActual.buscarPorCampoExacto(campo, valor);
        System.out.println("--- Resultados encontrados (" + encontrados.size() + ") ---");
        for (Documento d : encontrados) {
            System.out.println("ID " + d.getId() + " -> " + d.getDatos());
        }
    }

    /**
     * Ejecuta BUSCAR_RANGO idMin idMax.
     * Formato esperado: <id_min> <id_max>  Ejemplo: BUSCAR_RANGO 1 10
     * Aprovecha la complejidad O(log n + m) del arbol AVL.
     *
     * @param argumentos Cadena con el ID minimo y maximo separados por espacio.
     */
    private void ejecutarBuscarRango(String argumentos) {
        String[] partes = argumentos.split("\\s+");
        if (partes.length < 2)
            throw new IllegalArgumentException("Uso correcto: BUSCAR_RANGO <id_min> <id_max>");

        Integer min = Integer.parseInt(partes[0]);
        Integer max = Integer.parseInt(partes[1]);

        List<Documento> encontrados = coleccionActual.buscarPorRangoId(min, max);
        System.out.println("--- Documentos en rango [" + min + " - " + max + "] (ordenados por AVL) ---");
        for (Documento d : encontrados) {
            System.out.println("ID " + d.getId() + " -> " + d.getDatos());
        }
    }

    /**
     * Ejecuta ELIMINAR por ID.
     * Formato esperado: <id>  Ejemplo: ELIMINAR 1
     *
     * @param argumentos Cadena con el ID a eliminar.
     */
    private void ejecutarEliminar(String argumentos) {
        if (argumentos.isEmpty())
            throw new IllegalArgumentException("Debes proveer un ID para eliminar.");

        Integer id = Integer.parseInt(argumentos.trim());
        if (coleccionActual.eliminarPorId(id)) {
            System.out.println("OK: Documento [" + id + "] eliminado del AVL y del archivo JSON.");
        } else {
            System.out.println("ERROR: El ID [" + id + "] no existia en esta coleccion.");
        }
    }
}