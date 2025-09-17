package controllers;

import views.VistaAnalizador;
import parser.GeneradorArbolSintactico;
import static parser.RevisarArbol.correctSyntaxTree;
import java.awt.Color;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import lexer.*;
import parser.*;

public class ControladorAnalizador {

    private final VistaAnalizador vista;
    private File archivoActual;

    private boolean enMain = false;
    private int nivelLlaves = 0;

    public ControladorAnalizador(VistaAnalizador vista) {
        this.vista = vista;
        inicializarEventos();
    }

    private void inicializarEventos() {
        vista.analyzeButton.addActionListener(e -> {
            try {
                analizarCodigo();
            } catch (IOException ex) {
                Logger.getLogger(ControladorAnalizador.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        vista.clearButton.addActionListener(e -> limpiarCampos());
        vista.loadButton.addActionListener(e -> cargarArchivo());
        vista.saveButton.addActionListener(e -> guardarArchivo());
        vista.saveAsButton.addActionListener(e -> guardarComo());

        vista.deshacerItem.addActionListener(e -> {
            if (vista.undoManager.canUndo()) {
                vista.undoManager.undo();
            }
        });

        vista.rehacerItem.addActionListener(e -> {
            if (vista.undoManager.canRedo()) {
                vista.undoManager.redo();
            }
        });

        vista.copiarItem.addActionListener(e -> vista.inputArea.copy());
        vista.cortarItem.addActionListener(e -> vista.inputArea.cut());
        vista.pegarItem.addActionListener(e -> vista.inputArea.paste());

        vista.salirItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(vista,
                    "¿Desea cerrar SpectrumIDE?", "Confirmar salida...",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                vista.dispose();
            }
        });
    }

    private void analizarCodigo() throws IOException {
        // Guardar el código fuente en archivo
        File archivo = new File("archivo.txt");
        try (PrintWriter escribir = new PrintWriter(archivo)) {
            escribir.print(vista.inputArea.getText());
        }

        // Guardar una copia del código en copia.txt
        File copiaArchivo = new File("copia.txt");
        try (PrintWriter escribirCopia = new PrintWriter(copiaArchivo)) {
            escribirCopia.print(vista.inputArea.getText());
            System.out.println("Copia del archivo guardada correctamente en " + copiaArchivo.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error al guardar copia.txt: " + e.getMessage());
        }

        // Crear el lector para analizar el código
        Reader lector = new BufferedReader(new FileReader("archivo.txt"));
        Lexer lexer = new Lexer(lector);
        DefaultTableModel modeloTabla = vista.getModeloTabla();
        modeloTabla.setRowCount(0);

        StringBuilder resultado = new StringBuilder();
        int reservadas = 0;
        int identificadores = 0;
        int maxLinea = 0;

        Tokens tokenActual;
        while ((tokenActual = lexer.yylex()) != null) {
            String lexema = lexer.lexema;
            int linea = lexer.linea;
            int columna = lexer.columna;
            String grupo = obtenerGrupo(tokenActual);
            String alcance = obtenerAlcance(tokenActual, lexema);

            String valor = "-";
            if (tokenActual == Tokens.IDENTIFICADOR) {
                valor = obtenerValor(tokenActual, lexema);
            }

            Token token = new Token(tokenActual, lexema, linea, columna, grupo, alcance, valor);
            agregarTokenATabla(token);

            resultado.append('<')
                    .append(tokenActual).append(",  ")
                    .append(lexema).append(">\n");

            if (tokenActual == Tokens.IDENTIFICADOR) {
                identificadores++;
            } else if (esPalabraReservada(lexema)) {
                reservadas++;
            }

            if (linea > maxLinea) {
                maxLinea = linea;
            }
        }

        resultado.append("EOF");
        vista.outputArea.setText(resultado.toString());
        vista.lblReservadas.setText("Palabras reservadas: " + reservadas);
        vista.lblIdentificadores.setText("Identificadores: " + identificadores);
        vista.lblLineas.setText("Líneas: " + maxLinea);

        String codigoFuente = vista.inputArea.getText();
        Sintax parser = new Sintax(new parser.LexerCup(new StringReader(codigoFuente)));

        try {

            parser.parse();

            vista.sintacticoErroresArea.setForeground(new Color(25, 111, 61));
            vista.sintacticoErroresArea.setText("Análisis sintáctico completado sin errores.");

            String archivoSalida = "arbol.txt";
            GeneradorArbolSintactico generador = new GeneradorArbolSintactico();
            generador.generarDesdeArchivo("archivo.txt", archivoSalida);
            correctSyntaxTree(archivoSalida);

            String arbolSintactico = new String(Files.readAllBytes(Paths.get(archivoSalida)));
            vista.sintacticoArbolArea.setText(arbolSintactico);

        } catch (RuntimeException ex) {

            vista.sintacticoErroresArea.setForeground(Color.RED);

            String errorMessage = ex.getMessage();
            System.err.println(errorMessage);

            vista.sintacticoErroresArea.setText(errorMessage);
            vista.sintacticoArbolArea.setText("No se generó árbol sintáctico debido a errores de sintaxis.");

        } catch (Exception ex) {
            vista.sintacticoErroresArea.setForeground(Color.RED);
            vista.sintacticoErroresArea.setText("Error fatal durante el análisis: " + ex.getMessage());
            System.err.println(ex.getMessage());
        }
    }

    private String obtenerValor(Tokens token, String lexema) {
        File copiaArchivo = new File("copia.txt");
        String valor = "VNA";

        if (!copiaArchivo.exists()) {
            System.out.println("No se encontró copia.txt");
            return valor;
        }

        try {
            String contenido = new String(Files.readAllBytes(copiaArchivo.toPath())).replaceAll("\\s+", "");

            Pattern pattern = Pattern.compile(Pattern.quote(lexema) + "=(.*?)(;|$)");
            Matcher matcher = pattern.matcher(contenido);

            if (matcher.find()) {
                valor = matcher.group(1);

                // Limpiar el valor si está entre comillas
                if (valor.startsWith("\"") && valor.endsWith("\"")) {
                    valor = valor.substring(1, valor.length() - 1);
                } else if (valor.startsWith("'") && valor.endsWith("'")) {
                    valor = valor.substring(1, valor.length() - 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return valor;
    }

    private String obtenerGrupo(Tokens token) {
        return switch (token) {
            case INT, CHAR, FLOAT, BOOL, STRING, VOID, CONST, BREAK, CONTINUE, FOR, WHILE, DO, IF, ELSE, SWITCH, CASE, DEFAULT, RETURN, TRUE, FALSE, INPUT, PRINT, MAIN, LEN ->
                "Palabra reservada";
            case IDENTIFICADOR ->
                "Identificador";
            case LITERAL_INT, LITERAL_FLOAT, LITERAL_CHAR, LITERAL_STRING ->
                "Literal";
            case SUMA, RESTA, MULT, DIV, MOD, INCREMENTO, DECREMENTO ->
                "Operador aritmético";
            case IGUALDAD, DESIGUALDAD, MENOR, MAYOR, MENORIGUAL, MAYORIGUAL ->
                "Operador relacional";
            case AND, OR, NOT ->
                "Operador lógico";
            case ASIGNACION ->
                "Asignación";
            case COMENTARIO_BLOQUE, COMENTARIO_LINEA ->
                "Comentario";
            case IZQ_PAREN, DERECHA_PAREN, IZQ_LLAVE, DERECHA_LLAVE, IZQ_CORCHETE, DERECHA_CORCHETE ->
                "Delimitador";
            case PUNTO_Y_COMA, COMA, DOS_PUNTOS ->
                "Separador";
            case ERROR ->
                "Error";
            default ->
                "Desconocido";
        };
    }

    private boolean esPalabraReservada(String lexema) {
        return switch (lexema) {
            case "int", "char", "float", "bool", "string", "void", "const", "break", "continue", "for", "while", "do", "if", "else", "switch", "case", "default", "return", "true", "false", "input", "print", "main", "len" ->
                true;
            default ->
                false;
        };
    }

    private String obtenerAlcance(Tokens token, String lexema) {
        if (token == Tokens.MAIN) {
            enMain = true;
        } else if (token == Tokens.IZQ_LLAVE && enMain) {
            nivelLlaves++;
        } else if (token == Tokens.DERECHA_LLAVE && enMain) {
            nivelLlaves--;
            if (nivelLlaves == 0) {
                enMain = false;
            }
        }
        if (token == Tokens.IDENTIFICADOR) {
            return enMain ? "local" : "global";
        }
        return "-";
    }

    private void agregarTokenATabla(Token token) {
        DefaultTableModel modelo = vista.getModeloTabla();
        modelo.addRow(token.toTableRow());
    }

    private void cargarArchivo() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
            archivoActual = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(archivoActual))) {
                vista.inputArea.setText(reader.lines().reduce("", (a, b) -> a + b + "\n"));
                vista.setTitle("Analizador Léxico - " + archivoActual.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(vista, "Error al cargar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarArchivo() {
        if (archivoActual == null) {
            guardarComo();
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivoActual))) {
                writer.write(vista.inputArea.getText());
                vista.setTitle("Analizador Léxico - " + archivoActual.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(vista, "Error al guardar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarComo() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(vista) == JFileChooser.APPROVE_OPTION) {
            archivoActual = fileChooser.getSelectedFile();
            guardarArchivo();
        }
    }

    private void limpiarCampos() {
        vista.inputArea.setText("");
        vista.outputArea.setText("");
        vista.sintacticoErroresArea.setText("");
        vista.sintacticoArbolArea.setText("");
        vista.getModeloTabla().setRowCount(0);
        vista.lblReservadas.setText("Palabras reservadas: 0");
        vista.lblIdentificadores.setText("Identificadores: 0");
        vista.lblLineas.setText("Líneas: 0");
        archivoActual = null;
        vista.actualizarTituloVentana(null);
    }

}
