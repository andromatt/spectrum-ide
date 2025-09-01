package lexer;

public class Token {

    private Tokens token;
    private String lexema;
    private int linea;
    private int columna;
    private String grupo;
    private String alcance;
    private String valor;

    public Token(Tokens token, String lexema, int linea, int columna, String grupo, String alcance, String valor) {
        this.token = token;
        this.lexema = lexema;
        this.linea = linea;
        this.columna = columna;
        this.grupo = grupo;
        this.alcance = alcance;
        this.valor = valor;
    }

    public Tokens getToken() {
        return token;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getGrupo() {
        return grupo;
    }

    public String getAlcance() {
        return alcance;
    }

    public String getValor() {
        return valor;
    }

    public Object[] toTableRow() {
        return new Object[]{this.token, this.lexema, this.linea, this.columna, this.grupo, this.alcance, this.valor};
    }

    @Override
    public String toString() {
        return "Token {"
                + " token = " + token
                + ", lexema = '" + lexema
                + ", linea = " + linea
                + ", columna = " + columna
                + " }";
    }
}
