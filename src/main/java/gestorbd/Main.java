package gestorbd;

import com.gestorbd.interfazusuario.GuiBaseDatos;
import com.gestorbd.motor.GestorColecciones;

import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GestorColecciones gestorColecciones = new GestorColecciones("data_db");
            GuiBaseDatos ventana = new GuiBaseDatos(gestorColecciones);
            ventana.setVisible(true);
        });
    }
}