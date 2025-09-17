package parser;

import java_cup.runtime.Symbol;
import static parser.Sym.*;

%%
%class LexerCup
%public
%cup
%line
%column

// --- [ Expresiones Regulares] ---
DIGITO = [0-9]
LETRA = [a-zA-Z]
GUION_BAJO = [_]

ID = ({LETRA})({LETRA}|{DIGITO}|{GUION_BAJO})* | ({GUION_BAJO})({LETRA}|{DIGITO}|{GUION_BAJO})+

LITERAL_INT = {DIGITO}+
LITERAL_FLOAT   = {DIGITO}+\.{DIGITO}+
LITERAL_CHAR = \' ( [^'\\\n\r] | \\ ['] | \\\\ ) \'
LITERAL_STRING = \" ( [^\"\\\n\r] | \\ ( [n\"] | \\ ) )* \"

ESPACIOS = [ \t\r\n]+
COMENTARIO_LINEA   = "//".*
COMENTARIO_BLOQUE  = "/\*" [^*]* "*"+ ([^/*][^*]* "*"+)* "/" | "/\*" "*/"

%{
    private Symbol symbol(int type, Object value){
        return new Symbol(type, yyline + 1, yycolumn + 1, value);
    }
    private Symbol symbol(int type){
        return new Symbol(type, yyline + 1, yycolumn + 1);
    }
%}

%%

// --- [ Reglas Léxicas ] ---

{ESPACIOS}        { /* Ignorar */ }
{COMENTARIO_LINEA}  { /* Ignorar */ }
{COMENTARIO_BLOQUE} { /* Ignorar */ }

// Palabras Reservadas
"int"       { return symbol(INT, yytext()); }
"char"      { return symbol(CHAR, yytext()); }
"float"     { return symbol(FLOAT, yytext()); }
"bool"      { return symbol(BOOL, yytext()); }
"string"    { return symbol(STRING, yytext()); }
"void"      { return symbol(VOID, yytext()); }
"const"     { return symbol(CONST, yytext()); }
"break"     { return symbol(BREAK, yytext()); }
"continue"  { return symbol(CONTINUE, yytext()); }
"for"       { return symbol(FOR, yytext()); }
"while"     { return symbol(WHILE, yytext()); }
"do"        { return symbol(DO, yytext()); }
"if"        { return symbol(IF, yytext()); }
"else"      { return symbol(ELSE, yytext()); }
"switch"    { return symbol(SWITCH, yytext()); }
"case"      { return symbol(CASE, yytext()); }
"default"   { return symbol(DEFAULT, yytext()); }
"return"    { return symbol(RETURN, yytext()); }
"true"      { return symbol(TRUE, yytext()); }
"false"     { return symbol(FALSE, yytext()); }
"input"     { return symbol(INPUT, yytext()); }
"print"     { return symbol(PRINT, yytext()); }
"main"      { return symbol(MAIN, yytext()); }
"len"       { return symbol(LEN, yytext()); }

// Identificadores y Literales
{ID}                { return symbol(IDENTIFICADOR, yytext()); }
{LITERAL_INT}       { return symbol(LITERAL_INT, yytext()); }
{LITERAL_FLOAT}     { return symbol(LITERAL_FLOAT, yytext()); }
{LITERAL_CHAR}      { return symbol(LITERAL_CHAR, yytext()); }
{LITERAL_STRING}    { return symbol(LITERAL_STRING, yytext()); }

// Operadores
"++" { return symbol(INCREMENTO); }
"--" { return symbol(DECREMENTO); }
"==" { return symbol(IGUALDAD); }
"!=" { return symbol(DESIGUALDAD); }
"<=" { return symbol(MENORIGUAL); }
">=" { return symbol(MAYORIGUAL); }
"<"  { return symbol(MENOR); }
">"  { return symbol(MAYOR); }
"&&" { return symbol(AND); }
"||" { return symbol(OR); }
"!"  { return symbol(NOT); }
"="  { return symbol(ASIGNACION); }
"+"  { return symbol(SUMA); }
"-"  { return symbol(RESTA); }
"*"  { return symbol(MULT); }
"/"  { return symbol(DIV); }
"%"  { return symbol(MOD); }

// Símbolos y Separadores
"(" { return symbol(IZQ_PAREN); }
")" { return symbol(DERECHA_PAREN); }
"{" { return symbol(IZQ_LLAVE); }
"}" { return symbol(DERECHA_LLAVE); }
"[" { return symbol(IZQ_CORCHETE); }
"]" { return symbol(DERECHA_CORCHETE); }
";" { return symbol(PUNTO_Y_COMA); }
":" { return symbol(DOS_PUNTOS); }
"," { return symbol(COMA); }

. { return symbol(Sym.error, yytext()); }
