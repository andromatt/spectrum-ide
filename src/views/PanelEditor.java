package views;

import org.fife.ui.autocomplete.*;
import org.fife.ui.rtextarea.SearchContext;

import javax.swing.text.DefaultHighlighter;
import javax.swing.text.BadLocationException;
import java.awt.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.SearchEngine;

public class PanelEditor extends RSyntaxTextArea {

    public PanelEditor() {
        super(20, 60);
        setSyntaxEditingStyle(SYNTAX_STYLE_C);
        aplicarTema("monokai");
        setCodeFoldingEnabled(true);

        setMarkOccurrences(true); // Resaltar ocurrencias de palabras
        setHighlightCurrentLine(true); // Resaltar línea actual

        setCodeFoldingEnabled(true);
        crearProvider();

        addCaretListener(e -> resaltarCoincidenciasSeleccionadas());
    }

    private void crearProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        String[] palabras = {
            "int", "float", "string", "char", "bool", "const", "void", "return",
            "continue", "break", "input", "print", "main", "len", "true", "false",
            "for", "do", "while", "if", "else", "switch", "case", "default"
        };
        for (String palabra : palabras) {
            provider.addCompletion(new BasicCompletion(provider, palabra));
        }

        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(this);
    }

    public void resaltarCoincidenciasSeleccionadas() {
        String seleccionado = getSelectedText();
        if (seleccionado != null && !seleccionado.isBlank()) {
            SearchContext context = new SearchContext();
            context.setSearchFor(seleccionado);
            context.setMatchCase(true);
            context.setWholeWord(true);
            SearchEngine.markAll(this, context);
        }
    }

    public void marcarLineaError(int linea) {
        try {
            if (linea >= 0 && linea < getLineCount()) {
                int startOffset = getLineStartOffset(linea);
                int endOffset = getLineEndOffset(linea);
                getHighlighter().addHighlight(startOffset, endOffset,
                        new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 102, 102)));
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void aplicarTema(String nombreTema) {
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                    "/org/fife/ui/rsyntaxtextarea/themes/" + nombreTema + ".xml"));
            theme.apply(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
