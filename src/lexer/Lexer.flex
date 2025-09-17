package lexer;

import static lexer.Tokens.*;

%%
%class Lexer
%public
%type Tokens
%line
%column

// --- [ Expresiones Regulares ] ---
ESPACIOS = [ \t\r\n]+
DIGITO = [0-9]
LITERAL_INT = {DIGITO}+
LITERAL_FLOAT   = {DIGITO}+\.{DIGITO}+
LETRA = [a-zA-Z]
GUION_BAJO = [_]
ID = ({LETRA})({LETRA}|{DIGITO}|{GUION_BAJO})* | ({GUION_BAJO})({LETRA}|{DIGITO}|{GUION_BAJO})+
LITERAL_CHAR = \' ( [^'\\\n\r] | \\ ['] | \\\\ ) \'
LITERAL_STRING = \" ( [^\"\\\n\r] | \\ ( [n\"] | \\ ) )* \"
COMENTARIO_LINEA   = "//".*
COMENTARIO_BLOQUE  = "/\*" [^*]* "*"+ ([^/*][^*]* "*"+)* "/" | "/\*" "*/"

%{
    public String lexema;
    public int linea;
    public int columna;
%}

%%

// --- [ Reglas Léxicas ] ---

{ESPACIOS}  { /* Ignorar */ }

// Comentarios
{COMENTARIO_LINEA}  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return COMENTARIO_LINEA; }
{COMENTARIO_BLOQUE} { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return COMENTARIO_BLOQUE; }

// Palabras Reservadas
"int"       { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return INT; }
"char"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return CHAR; }
"float"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return FLOAT; }
"bool"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return BOOL; }
"string"    { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return STRING; }
"void"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return VOID; }
"const"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return CONST; }
"break"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return BREAK; }
"continue"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return CONTINUE; }
"for"       { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return FOR; }
"while"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return WHILE; }
"do"        { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DO; }
"if"        { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return IF; }
"else"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return ELSE; }
"switch"    { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return SWITCH; }
"case"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return CASE; }
"default"   { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DEFAULT; }
"return"    { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return RETURN; }
"true"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return TRUE; }
"false"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return FALSE; }
"input"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return INPUT; }
"print"     { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return PRINT; }
"main"      { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MAIN; }
"len"       { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return LEN; }

// Literales e Identificadores
{ID}             { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return IDENTIFICADOR; }
{LITERAL_INT}    { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return LITERAL_INT; }
{LITERAL_FLOAT}  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return LITERAL_FLOAT; }
{LITERAL_CHAR}   { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return LITERAL_CHAR; }
{LITERAL_STRING} { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return LITERAL_STRING; }

// Operadores
"++" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return INCREMENTO; }
"--" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DECREMENTO; }
"==" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return IGUALDAD; }
"!=" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DESIGUALDAD; }
"<=" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MENORIGUAL; }
">=" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MAYORIGUAL; }
"<"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MENOR; }
">"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MAYOR; }
"&&" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return AND; }
"||" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return OR; }
"!"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return NOT; }
"="  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return ASIGNACION; }
"+"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return SUMA; }
"-"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return RESTA; }
"*"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MULT; }
"/"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DIV; }
"%"  { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return MOD; }

// Símbolos y Separadores
"(" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return IZQ_PAREN; }
")" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DERECHA_PAREN; }
"{" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return IZQ_LLAVE; }
"}" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DERECHA_LLAVE; }
"[" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return IZQ_CORCHETE; }
"]" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DERECHA_CORCHETE; }
";" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return PUNTO_Y_COMA; }
":" { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return DOS_PUNTOS; }
"," { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return COMA; }

// Regla de Error
. { lexema = yytext(); linea = yyline + 1; columna = yycolumn; return ERROR; }