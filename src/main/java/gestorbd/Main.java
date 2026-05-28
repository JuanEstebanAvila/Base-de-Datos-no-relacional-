package com.gestorbd;

import com.gestorbd.interfazusuario.ReplBaseDatos;
import com.gestorbd.motor.GestorColecciones;

public class Main {
    public static void main(String[] args) {
        GestorColecciones gestorColecciones = new GestorColecciones("data");
        new ReplBaseDatos(gestorColecciones).iniciar();
    }
}