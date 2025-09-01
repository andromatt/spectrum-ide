package spectrumide;

import vista.VistaAnalizador;
import com.formdev.flatlaf.IntelliJTheme;
import lexer.ControladorAnalizador;

public class SpectrumIDE {

    public static void main(String[] args) {
        try {
            // Establece el tema FlatLaf antes de crear las ventanas
            IntelliJTheme.setup(SpectrumIDE.class.getResourceAsStream(
                    "/temas/HighContrast.theme.json"));
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar FlatLaf: " + ex);
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            VistaAnalizador vista = new VistaAnalizador();
            ControladorAnalizador controlador = new ControladorAnalizador(vista);
            vista.setLocationRelativeTo(null);
            vista.setVisible(true);
        });
    }
}
