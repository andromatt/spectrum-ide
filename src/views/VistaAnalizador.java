package views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import views.PanelEditor;

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
    private JSplitPane splitPane;
    private JCheckBoxMenuItem verAnalisisItem;
    private int ultimaAlturaAnalisis = 300;
    private Gutter gutter;
    private int marcadorError = -1;

    public VistaAnalizador() {

        setTitle("Analizador Léxico - nuevo_proyecto");
        setSize(1040, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        Image icono = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/imgs/logo40px.png"));
        setIconImage(icono);

        analyzeButton = new JButton();
        clearButton = new JButton();
        loadButton = new JButton();
        saveButton = new JButton();
        saveAsButton = new JButton();

        inicializarMenus();
        
        inputArea = new PanelEditor();
        inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
        inputArea.setCodeFoldingEnabled(true);
        inputArea.setAntiAliasingEnabled(true);
        inputArea.setMargin(new Insets(5, 5, 5, 5));
        inputArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        try {
            aplicarTema("/org/fife/ui/rsyntaxtextarea/themes/vs.xml"); // tema por defecto
        } catch (Exception e) {
            e.printStackTrace();
        }

        inputArea.setFont(new Font("Consolas", Font.PLAIN, 16));

        RTextScrollPane sp = new RTextScrollPane(inputArea);
        sp.setLineNumbersEnabled(true);
        gutter = sp.getGutter();
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, 16));
        gutter.setLineNumberColor(Color.GRAY);

        JPanel panelInput = new JPanel(new BorderLayout());
        panelInput.add(sp, BorderLayout.CENTER);

        outputArea = new JTextArea(10, 60);
        outputArea.setEditable(false);

        sintacticoErroresArea = new JTextArea(10, 60);
        sintacticoErroresArea.setEditable(false);
        sintacticoErroresArea.setBorder(BorderFactory.createTitledBorder("Errores Sintácticos"));

        sintacticoErroresArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String linea = obtenerNumeroLineaSeleccionada();
                    if (linea != null) {
                        try {
                            int numLinea = Integer.parseInt(linea.trim()) - 1;
                            inputArea.setCaretPosition(inputArea.getDocument().getDefaultRootElement().getElement(numLinea).getStartOffset());
                            inputArea.requestFocus();
                            PanelEditor panel = new PanelEditor();
                            panel.marcarLineaError(numLinea);
                        } catch (Exception ex) {
                            System.err.println("No se pudo ir a la línea: " + ex.getMessage());
                        }
                    }
                }
            }
        });

        sintacticoArbolArea = new JTextArea(10, 60);
        sintacticoArbolArea.setEditable(false);
        sintacticoArbolArea.setBorder(BorderFactory.createTitledBorder("Árbol Sintáctico"));

        modeloTabla = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Token", "Lexema", "Línea", "Columna", "Grupo", "Alcance", "Valor"});
        tablaTokens = new JTable(modeloTabla);

        JPanel panelLexico = new JPanel(new GridLayout(1, 2));
        panelLexico.add(new JScrollPane(outputArea));
        panelLexico.add(new JScrollPane(tablaTokens));

        JPanel panelSintactico = new JPanel(new GridLayout(1, 2));
        panelSintactico.add(new JScrollPane(sintacticoErroresArea));
        panelSintactico.add(new JScrollPane(sintacticoArbolArea));

        JTabbedPane tabbedPaneSalida = new JTabbedPane();
        tabbedPaneSalida.addTab("Análisis Léxico", panelLexico);
        tabbedPaneSalida.addTab("Análisis Sintáctico", panelSintactico);

        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelInput, tabbedPaneSalida);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(400);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblReservadas = new JLabel("Palabras reservadas: 0");
        lblIdentificadores = new JLabel("Identificadores: 0");
        lblLineas = new JLabel("Líneas: 0");

        panelEstado.add(lblReservadas);
        panelEstado.add(lblIdentificadores);
        panelEstado.add(lblLineas);
        panelEstado.setBackground(Color.BLACK);
        panelEstado.setForeground(Color.WHITE);
        add(panelEstado, BorderLayout.SOUTH);

        configurarAtajos();
    }

    // Aplicar tema
    private void aplicarTema(String path) throws IOException {
        InputStream in = getClass().getResourceAsStream(path);
        if (in != null) {
            Theme theme = Theme.load(in);
            theme.apply(inputArea);
        }
    }

    // Obtener línea desde el área de errores
    private String obtenerNumeroLineaSeleccionada() {
        try {
            int caret = sintacticoErroresArea.getCaretPosition();
            int start = sintacticoErroresArea.getLineStartOffset(sintacticoErroresArea.getLineOfOffset(caret));
            int end = sintacticoErroresArea.getLineEndOffset(sintacticoErroresArea.getLineOfOffset(caret));
            String texto = sintacticoErroresArea.getText().substring(start, end);
            // Supone que el error empieza con "Línea X: ..."
            if (texto.contains("Línea")) {
                return texto.replaceAll("[^0-9]", "");
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
/*
    // Resaltar error en gutter
    private void resaltarError(int linea) {
        try {
            if (marcadorError != -1) {
                gutter.removeTrackingIcon(marcadorError);
            }
            Icon icon = UIManager.getIcon("OptionPane.errorIcon");
            marcadorError = gutter.addLineTrackingIcon(linea, icon);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
*/
    
    // Atajos extra
    private void configurarAtajos() {
        // F5 → análisis rápido
        inputArea.getInputMap().put(KeyStroke.getKeyStroke("F6"), "analizar");
        inputArea.getActionMap().put("analizar", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                analyzeButton.doClick();
            }
        });

        
   
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

        // Archivo
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

        // Compilar
        JMenu menuCompilar = new JMenu("Compilar");
        JMenuItem analizarItem = new JMenuItem("Analizar");
        analizarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0));
        analizarItem.addActionListener(e -> analyzeButton.doClick());
        menuCompilar.add(analizarItem);

        // Editar
        JMenu menuEditar = new JMenu("Editar");
        deshacerItem = new JMenuItem("Deshacer");
        rehacerItem = new JMenuItem("Rehacer");
        copiarItem = new JMenuItem("Copiar");
        cortarItem = new JMenuItem("Cortar");
        pegarItem = new JMenuItem("Pegar");

        deshacerItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        rehacerItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        copiarItem.setAccelerator(KeyStroke.getKeyStroke("control C"));
        cortarItem.setAccelerator(KeyStroke.getKeyStroke("control X"));
        pegarItem.setAccelerator(KeyStroke.getKeyStroke("control V"));

        deshacerItem.addActionListener(e -> {
            if (undoManager.canUndo()) undoManager.undo();
        });
        rehacerItem.addActionListener(e -> {
            if (undoManager.canRedo()) undoManager.redo();
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

        // ✅ Ver (nuevo)
        JMenu menuVer = new JMenu("Ver");
        verAnalisisItem = new JCheckBoxMenuItem("Mostrar análisis", true);
        verAnalisisItem.addActionListener(e -> toggleAnalisis());
        menuVer.add(verAnalisisItem);

        // Cambiar tema
        JMenu temaSubMenu = new JMenu("Tema");
        JMenuItem monokaiItem = new JMenuItem("Monokai");
        JMenuItem eclipseItem = new JMenuItem("Eclipse");

        monokaiItem.addActionListener(e -> cambiarTema("/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
        eclipseItem.addActionListener(e -> cambiarTema("/org/fife/ui/rsyntaxtextarea/themes/eclipse.xml"));


        temaSubMenu.add(monokaiItem);
        temaSubMenu.add(eclipseItem);
        menuVer.add(temaSubMenu);

        // Cambiar tamaño de fuente
        JMenu fuenteSubMenu = new JMenu("Tamaño de fuente");
        JMenuItem aumentarItem = new JMenuItem("Aumentar");
        JMenuItem reducirItem = new JMenuItem("Reducir");

        aumentarItem.addActionListener(e -> cambiarFuente(2));
        reducirItem.addActionListener(e -> cambiarFuente(-2));

        fuenteSubMenu.add(aumentarItem);
        fuenteSubMenu.add(reducirItem);
        menuVer.add(fuenteSubMenu);

        // Ayuda
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
        menuBar.add(menuVer);
        menuBar.add(menuAyuda);

        setJMenuBar(menuBar);
    }

    // Mostrar/ocultar panel de análisis
    private void toggleAnalisis() {
        if (verAnalisisItem.isSelected()) {
            splitPane.setDividerLocation(ultimaAlturaAnalisis);
        } else {
            ultimaAlturaAnalisis = splitPane.getDividerLocation();
            splitPane.setDividerLocation(1.0);
        }
    }

    // Cambiar tema
    private void cambiarTema(String path) {
        try {
            aplicarTema(path);
            inputArea.repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cambiar fuente
    private void cambiarFuente(int delta) {
        Font actual = inputArea.getFont();
        int nuevoTam = Math.max(8, actual.getSize() + delta);
        inputArea.setFont(new Font(actual.getName(), actual.getStyle(), nuevoTam));
        gutter.setLineNumberFont(new Font("Consolas", Font.PLAIN, nuevoTam));
    }
}