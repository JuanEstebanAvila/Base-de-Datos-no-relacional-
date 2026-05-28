package gestorbd;

import com.gestorbd.interfazusuario.ReplBaseDatos;
import com.gestorbd.motor.GestorColecciones;

/**
 * Punto de entrada principal de la base de datos NoSQL.
 * Se encarga de inicializar el entorno de almacenamiento y arrancar
 * la interfaz de linea de comandos (REPL) para el usuario.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("[INFO] Iniciando motor de base de datos NoSQL...");

        // 1. Inicializamos el administrador global de colecciones.
        // Toda la base de datos guardara sus archivos .json dentro de esta carpeta.
        String carpetaAlmacenamiento = "data_db";
        GestorColecciones sistemaColecciones = new GestorColecciones(carpetaAlmacenamiento);

        System.out.println("[INFO] Repositorio de datos listo en la carpeta: /" + carpetaAlmacenamiento);

        // 2. Creamos el REPL y le pasamos el sistema de colecciones para que pueda operarlo.
        ReplBaseDatos consola = new ReplBaseDatos(sistemaColecciones);

        // 3. Encendemos el bucle infinito de la consola.
        // A partir de esta linea el programa se queda esperando los comandos del usuario.
        consola.iniciar();
    }
}