package spectrumide;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerarLexerAndParser {

    public static void main(String... args) throws Exception {
        generarLexer(rutaArchivoLexer, rutaArchivoLexerCup, rutas);
    }

    final static String rutaArchivoLexer = "C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\src\\lexer\\Lexer.flex";
    final static String rutaArchivoLexerCup = "C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\src\\parser\\LexerCup.flex";

    static final String[] rutas = {
        "-parser",
        "Sintax",
        "-symbols",
        "Sym",
        "C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\src\\parser\\Sintax.cup"
    };

    public static void generarLexer(String rutaArchivoFlex, String rutaArchivoLexerCup, String[] rutas) throws IOException, Exception {

        File archivo;

        archivo = new File(rutaArchivoFlex);
        JFlex.Main.generate(archivo);

        archivo = new File(rutaArchivoLexerCup);
        JFlex.Main.generate(archivo);

        java_cup.Main.main(rutas);

        // Mueve Sym.java
        Path pathSymOrigen = Paths.get("C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\Sym.java");
        Path pathSymDestino = Paths.get("C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\src\\parser\\Sym.java");

        if (Files.exists(pathSymDestino)) {
            Files.delete(pathSymDestino);
        }

        Files.move(pathSymOrigen, pathSymDestino);

        // Mueve Sintax.java
        Path pathSintaxOrigen = Paths.get("C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\Sintax.java");
        Path pathSintaxDestino = Paths.get("C:\\Users\\aleja\\OneDrive\\Documents\\NetBeansProjects\\SpectrumIDE\\src\\parser\\Sintax.java");

        if (Files.exists(pathSintaxDestino)) {
            Files.delete(pathSintaxDestino);
        }

        Files.move(pathSintaxOrigen, pathSintaxDestino);
    }
}
