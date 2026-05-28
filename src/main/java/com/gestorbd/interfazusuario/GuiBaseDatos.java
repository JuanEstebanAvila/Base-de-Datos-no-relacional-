package com.gestorbd.interfazusuario;

import com.gestorbd.modelo.Documento;
import com.gestorbd.motor.GestorColecciones;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Interfaz grafica minimalista del motor de base de datos NoSQL con arboles AVL.
 *
 * Organiza la pantalla en tres zonas:
 *   Zona 1 (izquierda superior) : Consola de comandos estilo terminal.
 *   Zona 2 (inferior)           : Documentos persistidos en la coleccion activa.
 *   Zona 3 (derecha superior)   : Estructura visual del arbol AVL en texto.
 *
 * Reutiliza ReplBaseDatos.procesarComando() para no duplicar logica de negocio.
 */
public class GuiBaseDatos extends JFrame {

    /** Instancia del REPL que procesa los comandos y mantiene el estado. */
    private final ReplBaseDatos repl;

    /** Gestor global de colecciones. */
    private final GestorColecciones gestorColecciones;

    // ── Componentes de la GUI 

    /** Selector desplegable con las colecciones disponibles. */
    private JComboBox<String> selectorColecciones;

    /** Zona 1: consola negra estilo terminal donde se muestran comandos y resultados. */
    private JTextArea areaConsola;

    /** Entrada de texto donde el usuario escribe los comandos. */
    private JTextField campoComando;

    /** Zona 2: muestra los documentos JSON de la coleccion activa. */
    private JTextArea areaContenidos;

    /** Zona 3: muestra la estructura del arbol AVL en texto estructurado. */
    private JTextArea areaArbolVisual;

    /**
     * Crea la ventana principal de la GUI.
     * @param gestorColecciones Gestor global de colecciones de la base de datos.
     */
    public GuiBaseDatos(GestorColecciones gestorColecciones) {
        this.gestorColecciones = gestorColecciones;
        this.repl              = new ReplBaseDatos(gestorColecciones);

        setTitle("Motor NoSQL - Indice AVL Minimalista");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicializarComponentes();
        refrescarSelectorColecciones();
    }

    // Construccion de la interfaz 

    /**
     * Construye y ensambla todos los componentes visuales de la ventana.
     */
    private void inicializarComponentes() {

        // Panel superior: gestion de colecciones
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        selectorColecciones = new JComboBox<>();
        JButton botonUse    = new JButton("USE (Cambiar coleccion)");
        JButton botonCrear  = new JButton("Crear coleccion");
        JButton botonBorrar = new JButton("Borrar coleccion");

        panelSuperior.add(new JLabel("Coleccion:"));
        panelSuperior.add(selectorColecciones);
        panelSuperior.add(botonUse);
        panelSuperior.add(botonCrear);
        panelSuperior.add(botonBorrar);

        // Zona 1: consola de comandos
        JPanel panelConsola = new JPanel(new BorderLayout());
        panelConsola.setBorder(BorderFactory.createTitledBorder("Consola de Comandos"));

        areaConsola = new JTextArea();
        areaConsola.setEditable(false);
        areaConsola.setBackground(Color.BLACK);
        areaConsola.setForeground(Color.GREEN);
        areaConsola.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaConsola.setText(
    "   BIENVENIDO AL MOTOR NOSQL AVL - INTERFAZ GRAFICA  \n" +
    "======================================================\n" +
    "Comandos globales:\n" +
    "  CREAR_COLECCION <nombre>\n" +
    "  BORRAR_COLECCION <nombre>\n" +
    "  LISTAR_COLECCIONES\n" +
    "  USE <nombre_coleccion>\n" +
    "\n" +
    "Comandos de datos (requiere USE primero):\n" +
    "  INSERTAR <id> <json>\n" +
    "  BUSCAR <id>\n" +
    "  BUSCAR DONDE <campo> = <valor>\n" +
    "  BUSCAR_RANGO <id_min> <id_max>\n" +
    "  ACTUALIZAR <id> <json>\n" +
    "  ELIMINAR <id>\n" +
    "\n" +
    "Ejemplos:\n" +
    "  INSERTAR 1 {\"nombre\":\"Juan\",\"edad\":25}\n" +
    "  BUSCAR 1\n" +
    "  BUSCAR DONDE nombre = \"Juan\"\n" +
    "  BUSCAR_RANGO 1 10\n" +
    "  ACTUALIZAR 1 {\"nombre\":\"Juan\",\"edad\":26}\n" +
    "  ELIMINAR 1\n" +
    "======================================================\n\n"
    );

        campoComando = new JTextField();
        campoComando.setFont(new Font("Monospaced", Font.PLAIN, 12));
        campoComando.setToolTipText("Escribe un comando y presiona Enter");

        JPanel panelEntrada = new JPanel(new BorderLayout());
        panelEntrada.add(new JLabel(" > "), BorderLayout.WEST);
        panelEntrada.add(campoComando, BorderLayout.CENTER);

        panelConsola.add(new JScrollPane(areaConsola), BorderLayout.CENTER);
        panelConsola.add(panelEntrada, BorderLayout.SOUTH);

        // Zona 3: visualizador del arbol AVL
        JPanel panelArbol = new JPanel(new BorderLayout());
        panelArbol.setBorder(BorderFactory.createTitledBorder("Estructura del Arbol AVL (RAM)"));

        areaArbolVisual = new JTextArea();
        areaArbolVisual.setEditable(false);
        areaArbolVisual.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaArbolVisual.setText("(sin coleccion activa)");

        panelArbol.add(new JScrollPane(areaArbolVisual), BorderLayout.CENTER);

        // ── Zona 2: visor de contenidos JSON
        areaContenidos = new JTextArea();
        areaContenidos.setEditable(false);
        areaContenidos.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaContenidos.setText("(sin coleccion activa)");

        JScrollPane scrollContenidos = new JScrollPane(areaContenidos);
        scrollContenidos.setBorder(BorderFactory.createTitledBorder("Documentos Persistidos (JSON)"));

        // ── Ensamble con divisores ─────────────────────────────────────────
        JSplitPane divisorSuperior = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, panelConsola, panelArbol);
        divisorSuperior.setDividerLocation(550);

        JSplitPane divisorPrincipal = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, divisorSuperior, scrollContenidos);
        divisorPrincipal.setDividerLocation(380);

        getContentPane().add(panelSuperior,   BorderLayout.NORTH);
        getContentPane().add(divisorPrincipal, BorderLayout.CENTER);

        // Eventos 
        campoComando.addActionListener(e -> ejecutarComandoDesdeGui());
        botonUse.addActionListener(e -> cambiarDeColeccion());
        botonCrear.addActionListener(e -> crearColeccionDesdeGui());
        botonBorrar.addActionListener(e -> borrarColeccionDesdeGui());
    }

    // Manejo de eventos 

    /**
     * Lee el comando del campo de texto, lo pasa al REPL y muestra el resultado
     * en la consola. Luego refresca las zonas 2 y 3.
     */
    private void ejecutarComandoDesdeGui() {
        String texto = campoComando.getText().trim();
        if (texto.isEmpty()) return;

        // Mostrar el comando en la consola
        areaConsola.append("> " + texto + "\n");

        // Procesar con el REPL y mostrar el resultado
        String resultado = repl.procesarComando(texto);
        areaConsola.append(resultado + "\n\n");

        // Hacer scroll automatico al final de la consola
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());

        campoComando.setText("");

        // Si el comando fue USE o CREAR_COLECCION, refrescar el selector
        String comando = texto.split("\\s+")[0].toUpperCase();
        if (comando.equals("USE") || comando.equals("CREAR_COLECCION") ||
            comando.equals("BORRAR_COLECCION")) {
            refrescarSelectorColecciones();
        }

        refrescarPantalla();
    }

    /**
     * Cambia la coleccion activa usando el selector desplegable.
     * Equivale a ejecutar USE <nombre> desde la consola.
     */
    private void cambiarDeColeccion() {
        String seleccion = (String) selectorColecciones.getSelectedItem();
        if (seleccion == null || seleccion.isEmpty()) return;

        String resultado = repl.procesarComando("USE " + seleccion);
        areaConsola.append("[SISTEMA] USE " + seleccion + "\n");
        areaConsola.append(resultado + "\n\n");
        areaConsola.setCaretPosition(areaConsola.getDocument().getLength());

        refrescarPantalla();
    }

    /**
     * Muestra un dialogo para ingresar el nombre de la nueva coleccion
     * y ejecuta CREAR_COLECCION si el usuario confirma.
     */
    private void crearColeccionDesdeGui() {
        String nombre = JOptionPane.showInputDialog(
                this, "Nombre de la nueva coleccion:", "Crear Coleccion",
                JOptionPane.PLAIN_MESSAGE);

        if (nombre != null && !nombre.trim().isEmpty()) {
            String resultado = repl.procesarComando("CREAR_COLECCION " + nombre.trim());
            areaConsola.append("[SISTEMA] CREAR_COLECCION " + nombre.trim() + "\n");
            areaConsola.append(resultado + "\n\n");
            areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
            refrescarSelectorColecciones();
        }
    }

    /**
     * Muestra un dialogo de confirmacion y ejecuta BORRAR_COLECCION
     * si el usuario acepta.
     */
    private void borrarColeccionDesdeGui() {
        String seleccion = (String) selectorColecciones.getSelectedItem();
        if (seleccion == null || seleccion.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona una coleccion para borrar.",
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "Seguro que deseas borrar la coleccion '" + seleccion + "'?\nEsta accion no se puede deshacer.",
                "Confirmar borrado",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String resultado = repl.procesarComando("BORRAR_COLECCION " + seleccion);
            areaConsola.append("[SISTEMA] BORRAR_COLECCION " + seleccion + "\n");
            areaConsola.append(resultado + "\n\n");
            areaConsola.setCaretPosition(areaConsola.getDocument().getLength());
            refrescarSelectorColecciones();
            refrescarPantalla();
        }
    }

    // ── Refresco de pantalla ───────────────────────────────────────────────

    /**
     * Actualiza la Zona 2 (documentos) y la Zona 3 (arbol AVL)
     * con el estado actual de la coleccion activa.
     * Complejidad: O(n) para recorrer todos los documentos.
     */
    public void refrescarPantalla() {
        if (repl.getColeccionActual() == null) {
            areaContenidos.setText("(sin coleccion activa)");
            areaArbolVisual.setText("(sin coleccion activa)");
            return;
        }

        // Zona 2: listar todos los documentos
        List<Documento> documentos = repl.getColeccionActual().obtenerTodosLosDocumentos();
        StringBuilder sbDocs = new StringBuilder();
        sbDocs.append("Coleccion: ").append(repl.getNombreColeccionActual())
              .append("  |  Total documentos: ").append(documentos.size()).append("\n");
        sbDocs.append("─".repeat(60)).append("\n");

        if (documentos.isEmpty()) {
            sbDocs.append("(coleccion vacia)");
        } else {
            for (Documento d : documentos) {
                sbDocs.append("ID ").append(d.getId())
                      .append("  ->  ").append(d.getDatos().toPrettyString())
                      .append("\n");
            }
        }
        areaContenidos.setText(sbDocs.toString());
        areaContenidos.setCaretPosition(0);

        // Zona 3: estructura del arbol AVL
        String estructuraArbol = repl.getColeccionActual().obtenerEstructuraArbolTexto();
        if (estructuraArbol == null || estructuraArbol.trim().isEmpty()) {
            areaArbolVisual.setText("(arbol vacio)");
        } else {
            areaArbolVisual.setText(estructuraArbol);
        }
        areaArbolVisual.setCaretPosition(0);
    }

    /**
     * Recarga el selector desplegable con las colecciones actuales.
     * Se llama cada vez que se crea o borra una coleccion.
     * Complejidad: O(n) donde n es el numero de colecciones.
     */
    private void refrescarSelectorColecciones() {
        DefaultComboBoxModel<String> modelo = new DefaultComboBoxModel<>(
                gestorColecciones.listarColecciones().toArray(new String[0]));
        selectorColecciones.setModel(modelo);
    }
}