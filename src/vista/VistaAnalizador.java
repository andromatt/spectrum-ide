package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import spectrumide.PanelEditor;

public class VistaAnalizador extends JFrame {

    public RSyntaxTextArea inputArea;
    public JTextArea outputArea;
    public JTextArea sintacticoErroresArea;
    public JTextArea sintacticoArbolArea;

    public JButton analyzeButton, clearButton, loadButton, saveButton, saveAsButton;
    public JLabel lblReservadas;
    public JLabel lblIdentificadores;
    public JLabel lblLineas;

    private final JTable tablaTokens;
    private final DefaultTableModel modeloTabla;

    public JMenuItem deshacerItem;
    public JMenuItem rehacerItem;
    public JMenuItem copiarItem;
    public JMenuItem cortarItem;
    public JMenuItem pegarItem;
    public JMenuItem salirItem;

    public UndoManager undoManager = new UndoManager();

    public VistaAnalizador() {

        setTitle("Analizador Léxico - nuevo_proyecto");
        setSize(1040, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Image icono = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/img/Spectrum_32px.png"));
        setIconImage(icono);

        analyzeButton = new JButton();
        clearButton = new JButton();
        loadButton = new JButton();
        saveButton = new JButton();
        saveAsButton = new JButton();

        inicializarMenus();

        // Panel de título
        // JPanel panelTitulo = new JPanel();
        // panelTitulo.setBackground(new Color(40, 40, 41));
        // panelTitulo.setLayout(new BorderLayout());
        // JLabel titulo = new JLabel("SPECTRUM IDE", SwingConstants.CENTER);
        // titulo.setForeground(Color.WHITE);
        // titulo.setFont(new Font("Arial", Font.BOLD, 22));
        // panelTitulo.add(titulo, BorderLayout.CENTER);
        // JPanel panelSuperior = new JPanel(new BorderLayout());
        // panelSuperior.add(panelTitulo, BorderLayout.CENTER);
        // add(panelSuperior, BorderLayout.NORTH);
        // Área de texto
        inputArea = new PanelEditor();
        inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
        inputArea.setCodeFoldingEnabled(true);
        inputArea.setAntiAliasingEnabled(true);
        inputArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        inputArea.setMargin(new Insets(5, 5, 5, 5));
        inputArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        
        try {
            String themePath = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";
            InputStream in = getClass().getResourceAsStream(themePath);
            Theme theme = Theme.load(in);
            theme.apply(inputArea);
        } catch (IOException e) {
            System.err.println("Tema del editor no encontrado: " + e.getMessage());
        }
        
        inputArea.setFont(new Font("Consolas", Font.PLAIN, 16));

        // Scroll pane con números de línea integrados
        RTextScrollPane sp = new RTextScrollPane(inputArea);
        sp.setLineNumbersEnabled(true);
        Gutter gutter = sp.getGutter();
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 16));
        gutter.setLineNumberColor(Color.GRAY);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(sp, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Áreas adicionales
        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);

        sintacticoErroresArea = new JTextArea(10, 60);
        sintacticoErroresArea.setEditable(false);
        sintacticoErroresArea.setBorder(BorderFactory.createTitledBorder("Errores Sintácticos"));

        sintacticoArbolArea = new JTextArea(10, 60);
        sintacticoArbolArea.setEditable(false);
        sintacticoArbolArea.setBorder(BorderFactory.createTitledBorder("Árbol Sintáctico"));

        // Tabla de tokens
        modeloTabla = new DefaultTableModel(new Object[][]{}, new String[]{"Token", "Lexema", "Línea", "Columna", "Grupo", "Alcance", "Valor"});
        tablaTokens = new JTable(modeloTabla);

        // Panel de entrada
        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.add(sp, BorderLayout.CENTER);
        panelInput.setPreferredSize(new Dimension(0, getHeight() / 2));

        // Panel de análisis léxico
        JScrollPane scrollOutput = new JScrollPane(outputArea);
        JScrollPane scrollTabla = new JScrollPane(tablaTokens);

        JPanel panelLexico = new JPanel(new GridLayout(1, 2));
        panelLexico.add(scrollOutput);
        panelLexico.add(scrollTabla);

        // Panel de análisis sintáctico
        JPanel panelSintactico = new JPanel(new GridLayout(1, 2));
        panelSintactico.add(new JScrollPane(sintacticoErroresArea));
        panelSintactico.add(new JScrollPane(sintacticoArbolArea));

        // Pestañas
        JTabbedPane tabbedPaneSalida = new JTabbedPane();
        tabbedPaneSalida.addTab("Análisis Léxico", panelLexico);
        tabbedPaneSalida.addTab("Análisis Sintáctico", panelSintactico);

        // Panel centro 
        JPanel panelCentro = new JPanel(new BorderLayout());
        panelCentro.add(panelInput, BorderLayout.NORTH);
        panelCentro.add(tabbedPaneSalida, BorderLayout.CENTER);

        add(panelCentro, BorderLayout.CENTER);

        // Panel de estado
        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblReservadas = new JLabel("Palabras reservadas: 0");
        lblIdentificadores = new JLabel("Identificadores: 0");
        lblLineas = new JLabel("Líneas: 0");

        panelEstado.add(lblReservadas);
        panelEstado.add(lblIdentificadores);
        panelEstado.add(lblLineas);
        panelEstado.setBackground(Color.BLACK);
        add(panelEstado, BorderLayout.SOUTH);
    }

    public void actualizarTituloVentana(String nombreArchivo) {
        if (nombreArchivo != null && !nombreArchivo.isEmpty()) {
            setTitle("Analizador Léxico - " + nombreArchivo);
        } else {
            setTitle("Analizador Léxico - nuevo_proyecto");
        }
    }

    public DefaultTableModel getModeloTabla() {
        return modeloTabla;
    }

    private void inicializarMenus() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");

        JMenuItem limpiarItem = new JMenuItem("Limpiar");
        JMenuItem cargarItem = new JMenuItem("Cargar");
        JMenuItem guardarItem = new JMenuItem("Guardar");
        JMenuItem guardarComoItem = new JMenuItem("Guardar como");
        salirItem = new JMenuItem("Salir");

        limpiarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        cargarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        guardarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        guardarComoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK));
        salirItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));

        limpiarItem.addActionListener(e -> clearButton.doClick());
        cargarItem.addActionListener(e -> loadButton.doClick());
        guardarItem.addActionListener(e -> saveButton.doClick());
        guardarComoItem.addActionListener(e -> saveAsButton.doClick());

        menuArchivo.add(limpiarItem);
        menuArchivo.add(cargarItem);
        menuArchivo.add(guardarItem);
        menuArchivo.add(guardarComoItem);
        menuArchivo.addSeparator();
        menuArchivo.add(salirItem);

        JMenu menuCompilar = new JMenu("Compilar");
        JMenuItem analizarItem = new JMenuItem("Analizar");
        analizarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        analizarItem.addActionListener(e -> analyzeButton.doClick());
        menuCompilar.add(analizarItem);

        JMenu menuEditar = new JMenu("Editar");
        deshacerItem = new JMenuItem("Deshacer");
        rehacerItem = new JMenuItem("Rehacer");
        copiarItem = new JMenuItem("Copiar");
        cortarItem = new JMenuItem("Cortar");
        pegarItem = new JMenuItem("Pegar");

        deshacerItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        rehacerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        copiarItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        cortarItem.setAccelerator(KeyStroke.getKeyStroke("control X"));
        pegarItem.setAccelerator(KeyStroke.getKeyStroke("control V"));

        deshacerItem.addActionListener(e -> {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        });

        rehacerItem.addActionListener(e -> {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        });

        copiarItem.addActionListener(e -> inputArea.copy());
        cortarItem.addActionListener(e -> inputArea.cut());
        pegarItem.addActionListener(e -> inputArea.paste());

        menuEditar.add(deshacerItem);
        menuEditar.add(rehacerItem);
        menuEditar.addSeparator();
        menuEditar.add(copiarItem);
        menuEditar.add(cortarItem);
        menuEditar.add(pegarItem);

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem manualItem = new JMenuItem("Manual de usuario");
        manualItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        manualItem.addActionListener(e -> new VisualizadorPDF().setVisible(true));

        JMenuItem acercaDeItem = new JMenuItem("Acerca de");
        acercaDeItem.addActionListener(e -> new VentanaAcercaDe());

        menuAyuda.add(manualItem);
        menuAyuda.add(acercaDeItem);

        menuBar.add(menuArchivo);
        menuBar.add(menuEditar);
        menuBar.add(menuCompilar);
        menuBar.add(menuAyuda);

        setJMenuBar(menuBar);
    }
}
