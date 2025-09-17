package spectrumide;

import views.VistaAnalizador;
import com.formdev.flatlaf.IntelliJTheme;
import controllers.ControladorAnalizador;

public class SpectrumIDE {

    public static void main(String[] args) {
        try {
            IntelliJTheme.setup(
                    SpectrumIDE.class.getResourceAsStream("/themes/Light.theme.json"));
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar el tema.");
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            VistaAnalizador vista = new VistaAnalizador();
            ControladorAnalizador controlador = new ControladorAnalizador(vista);
            vista.setLocationRelativeTo(null);
            vista.setVisible(true);
        });
    }
}
