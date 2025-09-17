package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GeneradorArbolSintactico {

    static class Nodo {

        String nombre;
        List<Nodo> hijos = new ArrayList<>();
        String valor;

        Nodo(String nombre) {
            this.nombre = nombre;
        }

        Nodo(String nombre, String valor) {
            this.nombre = nombre;
            this.valor = valor;
        }

        void agregarHijo(Nodo hijo) {
            hijos.add(hijo);
        }

        void imprimir(String prefijo, boolean esUltimo, BufferedWriter writer) throws IOException {
            writer.write(prefijo);
            if (esUltimo) {
                writer.write("└── ");
                prefijo += "    ";
            } else {
                writer.write("├── ");
                prefijo += "│   ";
            }
            if (valor != null) {
                writer.write(nombre + "(" + valor + ")");
            } else {
                writer.write(nombre);
            }
            writer.write("\n");
            for (int i = 0; i < hijos.size(); i++) {
                hijos.get(i).imprimir(prefijo, i == hijos.size() - 1, writer);
            }
        }
    }

    public void generarDesdeArchivo(String archivoEntrada, String archivoSalida) {
        try {
            List<String> lineas = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(archivoEntrada))) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    lineas.add(linea);
                }
            }

            Nodo arbol = parsearCodigo(lineas);

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivoSalida))) {
                arbol.imprimir("", true, bw);
            }

            System.out.println("Árbol sintáctico generado en: " + archivoSalida);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private Nodo parsearCodigo(List<String> lineas) {
        Nodo raiz = new Nodo("programa_raiz");
        Nodo listaDecl = new Nodo("lista_declaraciones");
        raiz.agregarHijo(listaDecl);

        for (int i = 0; i < lineas.size(); i++) {
            String lineaOriginal = lineas.get(i);

            String linea = lineaOriginal.split("//")[0].trim();

            if (linea.isEmpty() || linea.startsWith("/*")) {
                if (linea.startsWith("/*")) {
                    while (i < lineas.size() && !lineas.get(i).contains("*/")) {
                        i++;
                    }
                    if (i < lineas.size()) {
                        String restoLinea = lineas.get(i).split("\\*/", 2)[1].trim();
                        if (!restoLinea.isEmpty()) {
                            lineas.set(i, restoLinea);
                            i--;
                        }
                    }
                }
                continue;
            }

            if (linea.startsWith("int main()")) {
                Nodo decl = new Nodo("declaracion");
                Nodo declMain = new Nodo("declaracion_funcion_main");
                decl.agregarHijo(declMain);
                declMain.agregarHijo(new Nodo("INT"));
                declMain.agregarHijo(new Nodo("MAIN"));
                declMain.agregarHijo(new Nodo("IZQPAREN"));
                declMain.agregarHijo(new Nodo("DERECHAPAREN"));

                Nodo bloque = parsearBloque(lineas, i + 1);
                declMain.agregarHijo(bloque);

                listaDecl.agregarHijo(decl);
                i = encontrarFinBloque(lineas, i + 1);
            } else if (linea.matches("^(int|float|char|bool|string|void)\\s+\\w+\\s*\\(.*\\)\\s*\\{?")) {
                Nodo decl = parsearDeclaracionFuncion(lineas, i);
                listaDecl.agregarHijo(decl);
                i = encontrarFinBloque(lineas, i);
            } else if (linea.endsWith(";")) {
                Nodo sentencia = parsearSentencia(linea);
                if (sentencia != null) {
                    Nodo decl = new Nodo("declaracion");
                    decl.agregarHijo(sentencia);
                    listaDecl.agregarHijo(decl);
                }
            }
        }

        return raiz;
    }

    private int encontrarFinBloque(List<String> lineas, int startLine) {
        int nivel = 0;
        boolean dentroComentarioBloque = false;

        for (int i = startLine; i < lineas.size(); i++) {
            String lineaOriginal = lineas.get(i);
            String linea = lineaOriginal.split("//")[0].trim();

            if (dentroComentarioBloque) {
                if (linea.contains("*/")) {
                    dentroComentarioBloque = false;
                    linea = linea.split("\\*/", 2)[1].trim();
                } else {
                    continue;
                }
            } else if (linea.startsWith("/*")) {
                dentroComentarioBloque = true;
                if (linea.contains("*/")) {
                    dentroComentarioBloque = false;
                    linea = linea.split("\\*/", 2)[1].trim();
                } else {
                    continue;
                }
            }

            if (linea.isEmpty()) {
                continue;
            }

            int llaveAbierta = linea.indexOf('{');
            int llaveCerrada = linea.indexOf('}');

            if (llaveAbierta != -1) {
                nivel++;
            }
            if (llaveCerrada != -1) {
                nivel--;
                if (nivel == 0) {
                    return i;
                }
            }
        }
        return lineas.size() - 1;
    }

    private Nodo parsearDeclaracionFuncion(List<String> lineas, int startLine) {
        String primeraLinea = lineas.get(startLine).trim();
        Nodo decl = new Nodo("declaracion");
        Nodo declFunc = new Nodo("declaracion_otra_funcion");
        decl.agregarHijo(declFunc);

        String[] partes = primeraLinea.split("\\s+|\\(|\\)\\s*\\{?");
        declFunc.agregarHijo(new Nodo(partes[0].toUpperCase()));
        declFunc.agregarHijo(new Nodo("IDENTIFICADOR", partes[1]));
        declFunc.agregarHijo(new Nodo("IZQPAREN"));

        if (primeraLinea.contains("(") && primeraLinea.indexOf(")") > primeraLinea.indexOf("(")) {
            String paramsStr = primeraLinea.substring(primeraLinea.indexOf("(") + 1, primeraLinea.indexOf(")")).trim();
            if (!paramsStr.isEmpty()) {
                Nodo listaParams = new Nodo("lista_parametros");
                declFunc.agregarHijo(listaParams);
                for (String param : paramsStr.split(",")) {
                    param = param.trim();
                    String[] paramParts = param.split("\\s+");
                    Nodo paramNodo = new Nodo("parametro");
                    paramNodo.agregarHijo(new Nodo(paramParts[0].toUpperCase()));
                    paramNodo.agregarHijo(new Nodo("IDENTIFICADOR", paramParts[1]));
                    listaParams.agregarHijo(paramNodo);
                }
            }
        }
        declFunc.agregarHijo(new Nodo("DERECHAPAREN"));

        if (primeraLinea.endsWith("{")) {
            Nodo bloque = parsearBloque(lineas, startLine + 1);
            declFunc.agregarHijo(bloque);
        } else {
            for (int i = startLine + 1; i < lineas.size(); i++) {
                if (lineas.get(i).trim().equals("{")) {
                    Nodo bloque = parsearBloque(lineas, i + 1);
                    declFunc.agregarHijo(bloque);
                    break;
                }
            }
        }

        return decl;
    }

    private Nodo parsearBloque(List<String> lineas, int startLine) {
        Nodo bloque = new Nodo("bloque");
        bloque.agregarHijo(new Nodo("IZQLLAVE"));

        Nodo sentencias = new Nodo("sentencias_bloque");
        bloque.agregarHijo(sentencias);

        boolean dentroComentarioBloque = false;
        int i = startLine;

        while (i < lineas.size()) {
            String lineaOriginal = lineas.get(i);
            String linea = lineaOriginal.split("//")[0].trim();

            // Manejo de comentarios de bloque
            if (dentroComentarioBloque) {
                if (linea.contains("*/")) {
                    dentroComentarioBloque = false;
                    linea = linea.split("\\*/", 2)[1].trim();
                } else {
                    i++;
                    continue;
                }
            } else if (linea.startsWith("/*")) {
                dentroComentarioBloque = true;
                if (linea.contains("*/")) {
                    dentroComentarioBloque = false;
                    linea = linea.split("\\*/", 2)[1].trim();
                } else {
                    i++;
                    continue;
                }
            }

            if (linea.isEmpty()) {
                i++;
                continue;
            }

            if (linea.equals("}")) {
                bloque.agregarHijo(new Nodo("DERECHALLAVE"));
                break;
            }

            // Manejar estructuras de control
            if (linea.startsWith("if") || linea.startsWith("while") || linea.startsWith("for")
                    || linea.startsWith("switch") || linea.startsWith("do")) {

                Nodo estructura = parsearEstructuraControl(lineas, i);
                if (estructura != null) {
                    sentencias.agregarHijo(estructura);
                    i = encontrarFinBloque(lineas, i);
                }
            } else if (linea.endsWith(";")) {
                Nodo sentencia = parsearSentencia(linea);
                if (sentencia != null) {
                    sentencias.agregarHijo(sentencia);
                }
            }
            i++;
        }

        return bloque;
    }

    private Nodo parsearEstructuraControl(List<String> lineas, int startLine) {
        String lineaOriginal = lineas.get(startLine);
        String linea = lineaOriginal.split("//")[0].trim();

        if (linea.isEmpty()) {
            return null;
        }

        try {
            if (linea.startsWith("if")) {
                return parsearIf(lineas, startLine);
            } else if (linea.startsWith("while")) {
                return parsearWhile(lineas, startLine);
            } else if (linea.startsWith("for")) {
                return parsearFor(lineas, startLine);
            } else if (linea.startsWith("switch")) {
                return parsearSwitch(lineas, startLine);
            } else if (linea.startsWith("do")) {
                return parsearDoWhile(lineas, startLine);
            }
        } catch (Exception e) {
            System.err.println("Error al parsear estructura de control en línea " + (startLine + 1) + ": " + e.getMessage());
        }

        return null;
    }

    private Nodo parsearIf(List<String> lineas, int startLine) {
        String primeraLinea = lineas.get(startLine).trim();
        Nodo sentencia = new Nodo("sentencia");
        Nodo ifNodo = new Nodo("sentencia_if");
        sentencia.agregarHijo(ifNodo);

        ifNodo.agregarHijo(new Nodo("IF"));
        ifNodo.agregarHijo(new Nodo("IZQPAREN"));
        String condicion = primeraLinea.substring(primeraLinea.indexOf("(") + 1, primeraLinea.lastIndexOf(")"));
        ifNodo.agregarHijo(parsearExpresion(condicion));
        ifNodo.agregarHijo(new Nodo("DERECHAPAREN"));

        int currentLine = parsearBloqueIf(lineas, startLine, ifNodo);

        while (currentLine + 1 < lineas.size()) {
            String siguienteLinea = lineas.get(currentLine + 1).trim();

            if (siguienteLinea.startsWith("else if")) {
                Nodo elseIfPart = new Nodo("parte_else_if");
                ifNodo.agregarHijo(elseIfPart);
                elseIfPart.agregarHijo(new Nodo("ELSE IF"));
                elseIfPart.agregarHijo(new Nodo("IZQPAREN"));

                String condElseIf = siguienteLinea.substring(siguienteLinea.indexOf("(") + 1, siguienteLinea.lastIndexOf(")"));
                elseIfPart.agregarHijo(parsearExpresion(condElseIf));
                elseIfPart.agregarHijo(new Nodo("DERECHAPAREN"));

                currentLine = parsearBloqueIf(lineas, currentLine + 1, elseIfPart);
            } else if (siguienteLinea.equals("else")) {
                Nodo elsePart = new Nodo("parte_else");
                ifNodo.agregarHijo(elsePart);
                elsePart.agregarHijo(new Nodo("ELSE"));

                currentLine = parsearBloqueIf(lineas, currentLine + 1, elsePart);
                break;
            } else {
                break;
            }
        }

        return sentencia;
    }

    private int parsearBloqueIf(List<String> lineas, int startLine, Nodo nodoPadre) {
        String lineaActual = lineas.get(startLine).trim();

        if (lineaActual.endsWith("{")) {
            Nodo bloque = parsearBloque(lineas, startLine + 1);
            nodoPadre.agregarHijo(bloque);
            return encontrarFinBloque(lineas, startLine + 1);
        } else if (startLine + 1 < lineas.size() && lineas.get(startLine + 1).trim().equals("{")) {
            Nodo bloque = parsearBloque(lineas, startLine + 2);
            nodoPadre.agregarHijo(bloque);
            return encontrarFinBloque(lineas, startLine + 2);
        } else {
            Nodo bloque = new Nodo("bloque");
            bloque.agregarHijo(new Nodo("IZQLLAVE"));

            Nodo sentencias = new Nodo("sentencias_bloque");
            String contenido = lineaActual.substring(lineaActual.indexOf(")") + 1).trim();
            if (!contenido.isEmpty()) {
                Nodo sentencia = parsearSentencia(contenido + ";");
                if (sentencia != null) {
                    sentencias.agregarHijo(sentencia);
                }
            }

            bloque.agregarHijo(sentencias);
            bloque.agregarHijo(new Nodo("DERECHALLAVE"));
            nodoPadre.agregarHijo(bloque);
            return startLine;
        }
    }

    private Nodo parsearWhile(List<String> lineas, int startLine) {
        String primeraLinea = lineas.get(startLine).trim();
        Nodo sentencia = new Nodo("sentencia (sentencia_while)");
        Nodo whileNodo = new Nodo("sentencia_while");
        sentencia.agregarHijo(whileNodo);

        whileNodo.agregarHijo(new Nodo("WHILE"));
        whileNodo.agregarHijo(new Nodo("IZQPAREN"));

        String condicion = primeraLinea.substring(primeraLinea.indexOf("(") + 1, primeraLinea.lastIndexOf(")"));
        whileNodo.agregarHijo(parsearExpresion(condicion));
        whileNodo.agregarHijo(new Nodo("DERECHAPAREN"));

        if (primeraLinea.endsWith("{")) {
            Nodo bloque = parsearBloque(lineas, startLine + 1);
            whileNodo.agregarHijo(bloque);
        } else {
            for (int i = startLine + 1; i < lineas.size(); i++) {
                if (lineas.get(i).trim().equals("{")) {
                    Nodo bloque = parsearBloque(lineas, i + 1);
                    whileNodo.agregarHijo(bloque);
                    break;
                }
            }
        }

        return sentencia;
    }

    private Nodo parsearFor(List<String> lineas, int startLine) {
        String primeraLinea = lineas.get(startLine).trim();
        Nodo sentencia = new Nodo("sentencia (sentencia_for)");
        Nodo forNodo = new Nodo("sentencia_for");
        sentencia.agregarHijo(forNodo);

        forNodo.agregarHijo(new Nodo("FOR"));
        forNodo.agregarHijo(new Nodo("IZQPAREN"));

        // Parsear inicialización, condición y actualización
        String contenidoFor = primeraLinea.substring(primeraLinea.indexOf("(") + 1, primeraLinea.lastIndexOf(")"));
        String[] partesFor = contenidoFor.split(";");
        Nodo init = new Nodo("for_inicializacion");
        if (!partesFor[0].trim().isEmpty()) {
            if (partesFor[0].contains("=")) {
                init.agregarHijo(parsearSentencia(partesFor[0].trim() + ";"));
            } else {
                init.agregarHijo(parsearDeclaracionVariable(partesFor[0].trim()));
            }
        }
        forNodo.agregarHijo(init);
        forNodo.agregarHijo(new Nodo("PUNTOYCOMA"));

        Nodo cond = new Nodo("for_condicion");
        if (partesFor.length > 1 && !partesFor[1].trim().isEmpty()) {
            cond.agregarHijo(parsearExpresion(partesFor[1].trim()));
        }
        forNodo.agregarHijo(cond);
        forNodo.agregarHijo(new Nodo("PUNTOYCOMA"));

        Nodo act = new Nodo("for_actualizacion");
        if (partesFor.length > 2 && !partesFor[2].trim().isEmpty()) {
            act.agregarHijo(parsearSentencia(partesFor[2].trim() + ";"));
        }
        forNodo.agregarHijo(act);

        forNodo.agregarHijo(new Nodo("DERECHAPAREN"));

        if (primeraLinea.endsWith("{")) {
            Nodo bloque = parsearBloque(lineas, startLine + 1);
            forNodo.agregarHijo(bloque);
        } else {
            for (int i = startLine + 1; i < lineas.size(); i++) {
                if (lineas.get(i).trim().equals("{")) {
                    Nodo bloque = parsearBloque(lineas, i + 1);
                    forNodo.agregarHijo(bloque);
                    break;
                }
            }
        }

        return sentencia;
    }

    private Nodo parsearSwitch(List<String> lineas, int startLine) {
        String primeraLinea = lineas.get(startLine).trim();
        Nodo sentencia = new Nodo("sentencia (sentencia_switch)");
        Nodo switchNodo = new Nodo("sentencia_switch");
        sentencia.agregarHijo(switchNodo);

        switchNodo.agregarHijo(new Nodo("SWITCH"));
        switchNodo.agregarHijo(new Nodo("IZQPAREN"));

        String expr = primeraLinea.substring(primeraLinea.indexOf("(") + 1, primeraLinea.lastIndexOf(")"));
        switchNodo.agregarHijo(parsearExpresion(expr));
        switchNodo.agregarHijo(new Nodo("DERECHAPAREN"));
        switchNodo.agregarHijo(new Nodo("IZQLLAVE"));

        Nodo casos = new Nodo("lista_casos");
        switchNodo.agregarHijo(casos);

        int finSwitch = encontrarFinBloque(lineas, startLine);
        for (int i = startLine + 1; i < finSwitch; i++) {
            String linea = lineas.get(i).trim();
            if (linea.startsWith("case")) {
                Nodo caso = new Nodo("bloque_caso");
                casos.agregarHijo(caso);
                caso.agregarHijo(new Nodo("CASE"));

                String valorCase = linea.substring(linea.indexOf("case") + 4, linea.indexOf(":")).trim();
                caso.agregarHijo(new Nodo("literal", detectarTipoLiteral(valorCase) + ": " + valorCase));
                caso.agregarHijo(new Nodo("DOSPUNTOS"));

                Nodo sentenciasCaso = new Nodo("sentencias_bloque");
                caso.agregarHijo(sentenciasCaso);
                for (int j = i + 1; j < finSwitch; j++) {
                    String lineaCaso = lineas.get(j).trim();
                    if (lineaCaso.startsWith("case") || lineaCaso.startsWith("default") || lineaCaso.equals("}")) {
                        i = j - 1;
                        break;
                    }
                    if (!lineaCaso.isEmpty()) {
                        Nodo sentenciaCaso = parsearSentencia(lineaCaso);
                        if (sentenciaCaso != null) {
                            sentenciasCaso.agregarHijo(sentenciaCaso);
                        }
                    }
                }
            } else if (linea.startsWith("default")) {
                Nodo def = new Nodo("bloque_default");
                casos.agregarHijo(def);
                def.agregarHijo(new Nodo("DEFAULT"));
                def.agregarHijo(new Nodo("DOSPUNTOS"));

                // Parsear sentencias del default
                Nodo sentenciasDef = new Nodo("sentencias_bloque");
                def.agregarHijo(sentenciasDef);
                for (int j = i + 1; j < finSwitch; j++) {
                    String lineaDef = lineas.get(j).trim();
                    if (lineaDef.equals("}")) {
                        i = j - 1;
                        break;
                    }
                    if (!lineaDef.isEmpty()) {
                        Nodo sentenciaDef = parsearSentencia(lineaDef);
                        if (sentenciaDef != null) {
                            sentenciasDef.agregarHijo(sentenciaDef);
                        }
                    }
                }
            }
        }

        switchNodo.agregarHijo(new Nodo("DERECHALLAVE"));
        return sentencia;
    }

    private Nodo parsearDoWhile(List<String> lineas, int startLine) {
        Nodo sentencia = new Nodo("sentencia (sentencia_do_while)");
        Nodo doWhile = new Nodo("sentencia_do_while");
        sentencia.agregarHijo(doWhile);

        doWhile.agregarHijo(new Nodo("DO"));
        int inicioBloque = startLine + 1;
        while (inicioBloque < lineas.size() && !lineas.get(inicioBloque).trim().equals("{")) {
            inicioBloque++;
        }
        Nodo bloque = parsearBloque(lineas, inicioBloque + 1);
        doWhile.agregarHijo(bloque);
        int lineaWhile = encontrarFinBloque(lineas, inicioBloque) + 1;
        String whileLinea = lineas.get(lineaWhile).trim();
        doWhile.agregarHijo(new Nodo("WHILE"));
        doWhile.agregarHijo(new Nodo("IZQPAREN"));

        String condicion = whileLinea.substring(whileLinea.indexOf("(") + 1, whileLinea.lastIndexOf(")"));
        doWhile.agregarHijo(parsearExpresion(condicion));
        doWhile.agregarHijo(new Nodo("DERECHAPAREN"));
        doWhile.agregarHijo(new Nodo("PUNTOYCOMA"));

        return sentencia;
    }

    private Nodo parsearSentencia(String linea) {
        if (linea.endsWith(";")) {
            String sinPC = linea.substring(0, linea.length() - 1).trim();
            if (sinPC.startsWith("int ") || sinPC.startsWith("float ")
                    || sinPC.startsWith("char ") || sinPC.startsWith("bool ")
                    || sinPC.startsWith("string ")) {
                if (sinPC.contains("[") || sinPC.contains("{")) {
                    return parsearDeclaracionArreglo(sinPC);
                }
                return parsearDeclaracionVariable(sinPC);
            } else if (sinPC.startsWith("const ")) {
                return parsearDeclaracionConstante(sinPC);
            } else if (sinPC.startsWith("print(")) {
                return parsearSentenciaImprimir(sinPC);
            } else if (sinPC.startsWith("return")) {
                return parsearSentenciaReturn(sinPC);
            } else if (sinPC.matches("^\\w+\\s*\\(.*\\)$")) {
                return parsearLlamadaFuncion(sinPC);
            } else if (sinPC.matches("^\\w+\\s*(\\[.\\])?\\s=\\s*.+$")) {
                return parsearSentenciaAsignacion(sinPC);
            } else if (sinPC.matches("^\\w+(\\+\\+|--)$")
                    || sinPC.matches("^(\\+\\+|--)\\w+$")) {
                return parsearIncrementoDecremento(sinPC);
            }
        }
        return null;
    }

    private Nodo parsearDeclaracionVariable(String linea) {
        Nodo sentencia = new Nodo("sentencia (declaracion_variable PUNTOYCOMA)");
        Nodo declVar = new Nodo("declaracion_variable");
        sentencia.agregarHijo(declVar);

        String tipo = null, id = null, valor = null;
        String[] partes = linea.split("=", 2);

        if (partes.length == 2) {
            String izq = partes[0].trim();
            String der = partes[1].trim();

            String[] tokens = izq.split("\\s+");
            if (tokens.length == 2) {
                tipo = tokens[0];
                id = tokens[1];
            }
            valor = der;
        } else {
            String[] tokens = linea.split("\\s+");
            if (tokens.length == 2) {
                tipo = tokens[0];
                id = tokens[1];
            }
        }

        if (tipo != null) {
            declVar.agregarHijo(new Nodo(tipo.toUpperCase()));
        }
        if (id != null) {
            declVar.agregarHijo(new Nodo("IDENTIFICADOR", id));
        }
        if (valor != null) {
            declVar.agregarHijo(new Nodo("ASIGNACION"));
            declVar.agregarHijo(parsearExpresion(valor));
        }

        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearDeclaracionConstante(String linea) {
        Nodo sentencia = new Nodo("sentencia (declaracion_constante PUNTOYCOMA)");
        Nodo declConst = new Nodo("declaracion_constante");
        sentencia.agregarHijo(declConst);

        String[] partes = linea.substring(5).trim().split("=", 2);
        String izq = partes[0].trim();
        String der = partes.length > 1 ? partes[1].trim() : null;

        String[] tokens = izq.split("\\s+");
        declConst.agregarHijo(new Nodo("CONST"));
        declConst.agregarHijo(new Nodo(tokens[0].toUpperCase()));
        declConst.agregarHijo(new Nodo("IDENTIFICADOR", tokens[1]));

        if (der != null) {
            declConst.agregarHijo(new Nodo("ASIGNACION"));
            declConst.agregarHijo(parsearExpresion(der));
        }

        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearDeclaracionArreglo(String linea) {
        Nodo sentencia = new Nodo("sentencia (declaracion_arreglo PUNTOYCOMA)");
        Nodo declArr = new Nodo("declaracion_arreglo");
        sentencia.agregarHijo(declArr);
        String[] partesTipo = linea.split("\\s+", 2);
        String tipo = partesTipo[0];
        declArr.agregarHijo(new Nodo(tipo.toUpperCase()));
        String resto = partesTipo[1].trim();

        String identificador;
        int corcheteInicio = resto.indexOf("[");

        if (corcheteInicio != -1) {
            identificador = resto.substring(0, corcheteInicio).trim();
            declArr.agregarHijo(new Nodo("IDENTIFICADOR", identificador));

            Nodo dims = new Nodo("dimensiones_arreglo");
            declArr.agregarHijo(dims);

            while (corcheteInicio != -1) {
                int corcheteFin = resto.indexOf("]", corcheteInicio);
                String interior = resto.substring(corcheteInicio + 1, corcheteFin).trim();

                dims.agregarHijo(new Nodo("IZQCORCHETE"));

                if (!interior.isEmpty()) {
                    Nodo interiorNodo = new Nodo("interior");
                    if (interior.matches("^\\d+$")) {
                        interiorNodo.agregarHijo(new Nodo("LITERAL_INT", interior));
                    } else {
                        interiorNodo.agregarHijo(new Nodo("IDENTIFICADOR", interior));
                    }
                    dims.agregarHijo(interiorNodo);
                }

                dims.agregarHijo(new Nodo("DERECHACORCHETE"));
                corcheteInicio = resto.indexOf("[", corcheteFin);
            }
        }

        int igual = resto.indexOf("=");
        if (igual != -1) {
            String init = resto.substring(igual + 1).trim();
            declArr.agregarHijo(new Nodo("ASIGNACION"));

            Nodo initArr = new Nodo("inicializador_arreglo");
            declArr.agregarHijo(initArr);

            if (init.startsWith("{")) {
                initArr.agregarHijo(new Nodo("IZQLLAVE"));

                String elementos = init.substring(1, init.indexOf("}")).trim();
                if (!elementos.isEmpty()) {
                    Nodo listaElem = new Nodo("lista_elementos_arreglo");
                    initArr.agregarHijo(listaElem);

                    for (String elem : elementos.split(",")) {
                        elem = elem.trim();
                        Nodo elemNodo = new Nodo("lista_datos_arr");

                        // Determinar tipo de literal
                        if (elem.matches("^\\d+$")) {
                            elemNodo.agregarHijo(new Nodo("LITERAL_INT", elem));
                        } else if (elem.matches("^\\d+\\.\\d+$")) {
                            elemNodo.agregarHijo(new Nodo("LITERAL_FLOAT", elem));
                        } else if (elem.matches("^'.'$")) {
                            elemNodo.agregarHijo(new Nodo("LITERAL_CHAR", elem));
                        } else if (elem.startsWith("\"")) {
                            elemNodo.agregarHijo(new Nodo("LITERAL_STRING", elem));
                        } else if (elem.equals("true") || elem.equals("false")) {
                            elemNodo.agregarHijo(new Nodo("BOOL", elem));
                        } else {
                            elemNodo.agregarHijo(new Nodo("IDENTIFICADOR", elem));
                        }
                        listaElem.agregarHijo(elemNodo);
                    }
                }

                initArr.agregarHijo(new Nodo("DERECHALLAVE"));
            }
        }

        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearSentenciaImprimir(String linea) {
        Nodo sentencia = new Nodo("sentencia (sentencia_imprimir PUNTOYCOMA)");
        Nodo print = new Nodo("sentencia_imprimir");
        sentencia.agregarHijo(print);

        print.agregarHijo(new Nodo("PRINT"));
        print.agregarHijo(new Nodo("IZQPAREN"));

        Nodo listaArgs = new Nodo("lista_argumentos");
        print.agregarHijo(listaArgs);

        int start = linea.indexOf('(');
        int end = linea.lastIndexOf(')');
        if (start != -1 && end != -1 && end > start) {
            String args = linea.substring(start + 1, end);
            for (String arg : args.split(",")) {
                arg = arg.trim();
                listaArgs.agregarHijo(parsearExpresion(arg));
            }
        }

        print.agregarHijo(new Nodo("DERECHAPAREN"));
        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearSentenciaReturn(String linea) {
        Nodo sentencia = new Nodo("sentencia (sentencia_salto PUNTOYCOMA)");
        Nodo salto = new Nodo("sentencia_salto");
        sentencia.agregarHijo(salto);
        salto.agregarHijo(new Nodo("RETURN"));

        String[] partes = linea.split("\\s+", 2);
        if (partes.length > 1) {
            String val = partes[1].trim();
            salto.agregarHijo(parsearExpresion(val));
        }

        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearLlamadaFuncion(String linea) {
        Nodo sentencia = new Nodo("sentencia (llamada_funcion PUNTOYCOMA)");
        Nodo llamada = new Nodo("llamada_funcion");
        sentencia.agregarHijo(llamada);

        String[] partes = linea.split("\\(", 2);
        llamada.agregarHijo(new Nodo("IDENTIFICADOR", partes[0].trim()));
        llamada.agregarHijo(new Nodo("IZQPAREN"));

        Nodo listaArgs = new Nodo("lista_argumentos");
        llamada.agregarHijo(listaArgs);

        String args = partes[1].substring(0, partes[1].length() - 1).trim();
        if (!args.isEmpty()) {
            for (String arg : args.split(",")) {
                arg = arg.trim();
                listaArgs.agregarHijo(parsearExpresion(arg));
            }
        }

        llamada.agregarHijo(new Nodo("DERECHAPAREN"));
        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearSentenciaAsignacion(String linea) {
        Nodo sentencia = new Nodo("sentencia (sentencia_asignacion PUNTOYCOMA)");
        Nodo asignacion = new Nodo("sentencia_asignacion");
        sentencia.agregarHijo(asignacion);

        String[] partes = linea.split("=", 2);
        String izq = partes[0].trim();
        String der = partes[1].trim();

        if (izq.contains("[")) {
            Nodo acceso = new Nodo("acceso_arreglo");
            asignacion.agregarHijo(acceso);

            String id = izq.substring(0, izq.indexOf("[")).trim();
            acceso.agregarHijo(new Nodo("IDENTIFICADOR", id));

            Nodo indices = new Nodo("indices_arreglo");
            acceso.agregarHijo(indices);

            int corcheteInicio = izq.indexOf("[");
            while (corcheteInicio != -1) {
                int corcheteFin = izq.indexOf("]", corcheteInicio);
                String expr = izq.substring(corcheteInicio + 1, corcheteFin).trim();
                indices.agregarHijo(new Nodo("IZQCORCHETE"));
                indices.agregarHijo(parsearExpresion(expr));
                indices.agregarHijo(new Nodo("DERECHACORCHETE"));
                corcheteInicio = izq.indexOf("[", corcheteFin);
            }
        } else {
            asignacion.agregarHijo(new Nodo("IDENTIFICADOR", izq));
        }

        asignacion.agregarHijo(new Nodo("ASIGNACION"));
        asignacion.agregarHijo(parsearExpresion(der));

        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearIncrementoDecremento(String linea) {
        Nodo sentencia = new Nodo("sentencia (sentencia_asignacion PUNTOYCOMA)");
        Nodo asignacion = new Nodo("sentencia_asignacion");
        sentencia.agregarHijo(asignacion);

        if (linea.startsWith("++") || linea.startsWith("--")) {
            String op = linea.substring(0, 2);
            String id = linea.substring(2);
            asignacion.agregarHijo(new Nodo(op.equals("++") ? "INCREMENTO" : "DECREMENTO"));
            asignacion.agregarHijo(new Nodo("IDENTIFICADOR", id));
        } else {
            String op = linea.substring(linea.length() - 2);
            String id = linea.substring(0, linea.length() - 2);
            asignacion.agregarHijo(new Nodo("IDENTIFICADOR", id));
            asignacion.agregarHijo(new Nodo(op.equals("++") ? "INCREMENTO" : "DECREMENTO"));
        }

        sentencia.agregarHijo(new Nodo("PUNTOYCOMA"));
        return sentencia;
    }

    private Nodo parsearExpresion(String expr) {
        expr = expr.trim();
        Nodo expresion = new Nodo("expresion");

        // Orden de precedencia de operadores
        String[][] operadoresPorPrecedencia = {
            {"||"},
            {"&&"},
            {"==", "!="},
            {"<=", ">=", "<", ">"},
            {"+", "-"},
            {"*", "/", "%"}
        };
        for (String[] operadores : operadoresPorPrecedencia) {
            for (String op : operadores) {
                if (op.length() == 2) {
                    int index = expr.indexOf(op);
                    while (index != -1) {
                        if (index + op.length() <= expr.length()
                                && expr.substring(index, index + op.length()).equals(op)) {
                            boolean esOperadorCompleto = true;
                            if (index > 0) {
                                char previo = expr.charAt(index - 1);
                                if (Character.isLetterOrDigit(previo) || previo == ' ') {
                                    esOperadorCompleto = false;
                                }
                            }

                            if (esOperadorCompleto) {
                                String izquierda = expr.substring(0, index).trim();
                                String derecha = expr.substring(index + op.length()).trim();

                                Nodo opNodo = new Nodo("expresion_" + nombreOperador(op));
                                opNodo.agregarHijo(parsearExpresion(izquierda));
                                opNodo.agregarHijo(new Nodo(nombreOperadorCompleto(op)));
                                opNodo.agregarHijo(parsearExpresion(derecha));
                                expresion.agregarHijo(opNodo);
                                return expresion;
                            }
                        }
                        index = expr.indexOf(op, index + 1);
                    }
                }
            }
        }
        for (String[] operadores : operadoresPorPrecedencia) {
            for (String op : operadores) {
                if (op.length() == 1 && !op.equals("=")) {
                    int index = expr.indexOf(op);
                    while (index != -1) {
                        if (!(index + 1 < expr.length()
                                && (expr.charAt(index + 1) == '='
                                || (op.equals("-") && expr.charAt(index + 1) == '>')))) {

                            String izquierda = expr.substring(0, index).trim();
                            String derecha = expr.substring(index + 1).trim();

                            Nodo opNodo = new Nodo("expresion_" + nombreOperador(op));
                            opNodo.agregarHijo(parsearExpresion(izquierda));
                            opNodo.agregarHijo(new Nodo(nombreOperadorCompleto(op)));
                            opNodo.agregarHijo(parsearExpresion(derecha));
                            expresion.agregarHijo(opNodo);
                            return expresion;
                        }
                        index = expr.indexOf(op, index + 1);
                    }
                }
            }
        }

        // Manejar paréntesis
        if (expr.startsWith("(") && expr.endsWith(")")) {
            Nodo paren = new Nodo("expresion_primaria");
            paren.agregarHijo(new Nodo("IZQPAREN"));
            paren.agregarHijo(parsearExpresion(expr.substring(1, expr.length() - 1).trim()));
            paren.agregarHijo(new Nodo("DERECHAPAREN"));
            expresion.agregarHijo(paren);
            return expresion;
        }

        // Manejar literales e identificadores
        Nodo primaria = new Nodo("expresion_primaria");
        if (expr.matches("^\\d+\\.\\d+$")) {
            primaria.agregarHijo(new Nodo("LITERAL_FLOAT", expr));
        } else if (expr.matches("^\\d+$")) {
            primaria.agregarHijo(new Nodo("LITERAL_INT", expr));
        } else if (expr.matches("^'.'$")) {
            primaria.agregarHijo(new Nodo("LITERAL_CHAR", expr));
        } else if (expr.matches("^\".*\"$")) {
            primaria.agregarHijo(new Nodo("LITERAL_STRING", expr));
        } else if (expr.equals("true") || expr.equals("false")) {
            primaria.agregarHijo(new Nodo("BOOL", expr));
        } else if (expr.matches("^\\w+$")) {
            primaria.agregarHijo(new Nodo("IDENTIFICADOR", expr));
        }
        expresion.agregarHijo(primaria);
        return expresion;
    }

    private String nombreOperadorCompleto(String op) {
        switch (op) {
            case "+":
                return "SUMA";
            case "-":
                return "RESTA";
            case "*":
                return "MULTIPLICACION";
            case "/":
                return "DIVISION";
            case "%":
                return "MODULO";
            case "==":
                return "IGUALDAD";
            case "!=":
                return "DESIGUALDAD";
            case "<":
                return "MENOR";
            case ">":
                return "MAYOR";
            case "<=":
                return "MENORIGUAL";
            case ">=":
                return "MAYORIGUAL";
            case "&&":
                return "AND";
            case "||":
                return "OR";
            case "!":
                return "NOT";
            default:
                return op.toUpperCase();
        }
    }

    private String nombreOperador(String op) {
        switch (op) {
            case "||":
                return "logica_or";
            case "&&":
                return "logica_and";
            case "==":
            case "!=":
                return "igualdad";
            case "<":
            case ">":
            case "<=":
            case ">=":
                return "relacional";
            case "+":
            case "-":
                return "aditiva";
            case "*":
            case "/":
            case "%":
                return "multiplicativa";
            default:
                return "";
        }
    }

    private Nodo parsearAccesoArreglo(String expr) {
        Nodo acceso = new Nodo("acceso_arreglo");
        String id = expr.substring(0, expr.indexOf("[")).trim();
        acceso.agregarHijo(new Nodo("IDENTIFICADOR", id));

        Nodo indices = new Nodo("indices_arreglo");
        acceso.agregarHijo(indices);

        int corcheteInicio = expr.indexOf("[");
        while (corcheteInicio != -1) {
            int corcheteFin = expr.indexOf("]", corcheteInicio);
            String interior = expr.substring(corcheteInicio + 1, corcheteFin).trim();
            indices.agregarHijo(new Nodo("IZQCORCHETE"));
            indices.agregarHijo(parsearExpresion(interior));
            indices.agregarHijo(new Nodo("DERECHACORCHETE"));
            corcheteInicio = expr.indexOf("[", corcheteFin);
        }

        return acceso;
    }

    private Nodo parsearLlamadaLen(String expr) {
        Nodo llamada = new Nodo("llamada_len");
        llamada.agregarHijo(new Nodo("LEN"));
        llamada.agregarHijo(new Nodo("IZQPAREN"));

        String param = expr.substring(expr.indexOf("(") + 1, expr.indexOf(")")).trim();
        llamada.agregarHijo(new Nodo("IDENTIFICADOR", param));

        llamada.agregarHijo(new Nodo("DERECHAPAREN"));
        return llamada;
    }

    private Nodo parsearLlamadaInput(String expr) {
        Nodo llamada = new Nodo("llamada_input");
        llamada.agregarHijo(new Nodo("INPUT"));
        llamada.agregarHijo(new Nodo("IZQPAREN"));
        llamada.agregarHijo(new Nodo("DERECHAPAREN"));
        return llamada;
    }

    private Nodo parsearExpresionCasting(String expr) {
        Nodo casting = new Nodo("expresion_casting");
        String tipo = expr.substring(0, expr.indexOf("(")).trim();
        casting.agregarHijo(new Nodo(tipo.toUpperCase()));
        casting.agregarHijo(new Nodo("IZQPAREN"));

        String interior = expr.substring(expr.indexOf("(") + 1, expr.indexOf(")")).trim();
        casting.agregarHijo(parsearExpresion(interior));

        casting.agregarHijo(new Nodo("DERECHAPAREN"));
        return casting;
    }

    private String detectarTipoLiteral(String valor) {
        valor = valor.trim();
        if (valor.matches("^\\d+$")) {
            return "LITERAL_INT";
        }
        if (valor.matches("^\\d+\\.\\d+$")) {
            return "LITERAL_FLOAT";
        }
        if (valor.matches("^'.'$")) {
            return "LITERAL_CHAR";
        }
        if (valor.startsWith("\"") && valor.endsWith("\"")) {
            return "LITERAL_STRING";
        }
        if (valor.equals("true") || valor.equals("false")) {
            return "BOOL";
        }
        if (valor.matches("^\\w+$")) {
            return "IDENTIFICADOR";
        }
        return "DESCONOCIDO";
    }
}
