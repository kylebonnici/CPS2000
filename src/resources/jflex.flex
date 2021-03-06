import java_cup.runtime.*;

%%

%public
%class JFlexLexer
%cup
%line
%eofval{
  return new Symbol(JParserSym.EOF);
%eofval}

%{

      StringBuffer string = new StringBuffer();

      boolean useLineNumbers = true;


      private Symbol symbol(int type) {
        return new Symbol(type, yyline + 1, yycolumn);
      }
      private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline + 1, yycolumn, value);
      }

      private void dumpError() {
          System.out.println("Unexpected char ' " + yytext() + " ' " + (useLineNumbers? "on line "  + (yyline+1)  : "") );
      }
%}

newline=\r|\n|\r\n
whitespace={newline} | [ \t\f]

letter = [A-Za-z]
digit=[0-9]
printable = [\x20-\x7E]
type = "int" | "real" | "bool" | "char" | "string" | "unit"
booleanLiteral = "true" | "false"
integerLiteral = {digit}({digit})*
charLiteral = "'"{printable}?"'"
unitLiteral = "#"
realLiteral = {digit}({digit})* "." {digit}({digit})* (("e"|"E")("+"|"-") {digit}({digit})*)?
identifier = "_"|{letter}("_"|{letter}|{digit})*

relationalOp = "<"|">"|"=="|"!="|"<="|">="

%state STRING

%%

<YYINITIAL> {
    {newline} { }
    {whitespace} { /* do nothing */ }
    "*" { return symbol(JParserSym.MULTIPLICATION); }
    "/" { return symbol(JParserSym.DIVISION); }
    "and" { return symbol(JParserSym.AND); }
    "+" { return symbol(JParserSym.PLUS); }
    "-" { return symbol(JParserSym.MINUS); }
    "(" { return symbol(JParserSym.BRACE_OPEN); }
    "or" { return symbol(JParserSym.OR);}
    ")" { return symbol(JParserSym.BRACE_CLOSE); }
    "{" { return symbol(JParserSym.CURLY_BRACE_OPEN); }
    "}" { return symbol(JParserSym.CURLY_BRACE_CLOSE); }
    "," { return symbol(JParserSym.COMMA); }
    "<-" { return symbol(JParserSym.TO); }
    ";" { return symbol(JParserSym.SEMICOLON); }
    ":" { return symbol(JParserSym.COLON); }
    "=" { return symbol(JParserSym.EQ); }
    \"  {string.setLength(0); string.append( yytext() ); yybegin(STRING); }
    {relationalOp} { return symbol(JParserSym.RELATIONAL_OP,yytext()); }
    {type} { return symbol(JParserSym.TYPE,yytext()); }
    {booleanLiteral} { return symbol(JParserSym.BOOLEAN_LITERAL,new Boolean(yytext()));}
    {integerLiteral} { return symbol(JParserSym.INTEGER_LITERAL,new Integer(yytext()));}
    {charLiteral} { return symbol(JParserSym.CHAR_LITERAL, yytext());}
    {unitLiteral} { return symbol(JParserSym.UNIT_LITERAL,yytext());}
    {realLiteral} { return symbol(JParserSym.REAL_LITERAL, new Double(yytext()));}
    "set" { return symbol(JParserSym.SET);}
    "let" { return symbol(JParserSym.LET);}
    "in" { return symbol(JParserSym.IN);}
    "function" { return symbol(JParserSym.FUNCTION);}
    "not" { return symbol(JParserSym.NOT);}
    "else" { return symbol(JParserSym.ELSE);}
    "if" { return symbol(JParserSym.IF);}
    "halt" { return symbol(JParserSym.HALT);}
    "while" { return symbol(JParserSym.WHILE);}
    "read" { return symbol(JParserSym.READ);}
    "write" { return symbol(JParserSym.WRITE);}
    {identifier} { return symbol(JParserSym.IDENTIFIER,yytext()); }

}

<STRING> {
\"                             {string.append( yytext() ); yybegin(YYINITIAL);
                               return symbol(JParserSym.STRING_LITERAL,
                               string.toString()); }
[^\n\r\"\\]+                   { string.append( yytext() ); }
\\t                            { string.append('\t'); }
\\n                            { string.append('\n'); }

\\r                            { string.append('\r'); }
\\\"                           { string.append('\"'); }
\\                             { string.append('\\'); }
}

 [^] { dumpError(); }