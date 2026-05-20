package com.abarrotespro;
import com.abarrotespro.controlador.ControladorPrincipal;

/**
 * Punto de entrada de la aplicacion Abarrotes Pro.
 */
public class AbarrotesProApp {

    public static void main(String[] args) {
        System.out.println("Iniciando Abarrotes Pro V2.0...");
        // SistemaPos migra productos desde src/img a MySQL si la tabla esta vacia
        ControladorPrincipal controlador = new ControladorPrincipal();
        controlador.iniciar();
        //ANDIHBI
    }
}
