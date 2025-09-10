package views;

import javax.swing.*;
import java.awt.*;

public class VentanaAcercaDe extends JFrame {

    public VentanaAcercaDe() {

        setTitle("Acerca de");
        setSize(400, 250);
        setLocationRelativeTo(null);

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setText("""
                Spectrum IDE
                ANALIZADOR LEXICO Y SINTACTICO v0.1.2
                Equipo numero 3
                Año: 2025
                Lenguaje de desarrollo: Java SE
                Herramientas: JFlex y JCup
                Descripción: Esta herramienta permite analizar
                código fuente (.spect) para extraer tokens léxicos
                y mostrar estadísticas básicas del análisis lexico,
                ademas de realizar un analisis sintactico al codigo.
                """);
        info.setMargin(new Insets(10, 10, 10, 10));

        add(info);
        setVisible(true);
    }
}
