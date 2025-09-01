package vista;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File; // Ya no tan necesario para la lectura, pero puede quedarse
import java.io.IOException;
import java.io.InputStream; // Necesitarás este import

public class VisualizadorPDF extends JFrame {

    public VisualizadorPDF() {

        setTitle("Manual De Usuario");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Define la ruta relativa del PDF dentro de tu JAR
        // Asegúrate de que esta ruta coincida con donde colocaste el PDF
        // Si lo pusiste en Vista/resources/, la ruta sería "/Vista/resources/Manual_User_SpectrumIDE.pdf"
        // Si lo pusiste directamente en Vista/, la ruta sería "/Vista/Manual_User_SpectrumIDE.pdf"
        // La barra diagonal inicial (/) es importante para indicar la raíz del classpath.
        String pdfResourcePath = "/Vista/resources/Manual_User_SpectrumIDE.pdf"; // AJUSTA ESTA RUTA

        try {
            // Intenta cargar el recurso desde el classpath
            InputStream inputStream = getClass().getResourceAsStream(pdfResourcePath);

            if (inputStream == null) {
                // Si inputStream es null, el recurso no se encontró en la ruta especificada
                throw new IOException("No se encontró el recurso PDF en el JAR: " + pdfResourcePath +
                                      "\nVerifica la ruta en tu proyecto y que el archivo esté incluido.");
            }

            PDDocument document = PDDocument.load(inputStream); // Carga desde el InputStream
            PDFRenderer renderer = new PDFRenderer(document);

            JPanel panelPDF = new JPanel();
            panelPDF.setLayout(new BoxLayout(panelPDF, BoxLayout.Y_AXIS));

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 100);
                JLabel label = new JLabel(new ImageIcon(image));
                label.setAlignmentX(Component.CENTER_ALIGNMENT);
                panelPDF.add(label);
            }

            document.close();
            inputStream.close(); // Es importante cerrar el InputStream

            JScrollPane scroll = new JScrollPane(panelPDF);
            add(scroll, BorderLayout.CENTER);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al abrir el manual:\n" + e.getMessage(),
                    "Archivo no encontrado / Error de lectura", JOptionPane.ERROR_MESSAGE);
        }
    }
}