package mt.edu.um.cs.cps2000.lexer;

import java_cup.runtime.Symbol;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lexer implements java_cup.runtime.Scanner {
    private ArrayList<Symbol> tokens = new ArrayList<Symbol>();
    private ArrayList<Object[]> errorLog;
    private boolean done = false;
    int lineNumber = 0;
    int readerLineNumber = 0;
    private BufferedReader bufferToRead;
    private int tokenIndex = 0;
    public boolean useLineNumbers = false;

    public Symbol next_token(){
        if (tokens.size() == 0) try {
            lexIt();
        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }

        if (tokenIndex >= tokens.size()) return null;
        return tokens.get(tokenIndex++);
    }

    public Lexer(BufferedReader bufferToRead) throws IOException {
        this.bufferToRead = bufferToRead;
    }

    private Symbol symbol(int type) {
        return new Symbol(type, lineNumber,0);
    }
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, lineNumber, 0, value);
    }

    public ArrayList<Symbol> getTokens() {
        return tokens;
    }
    private static int EOF = -1;


    //-----------------------------Keyword-------------------------------------

    private boolean isKeyword(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("set")) {
            tokens.add(symbol(JParserSym.SET,str.toString()));
        }else if (str.toString().equals("let")) {
            tokens.add(symbol(JParserSym.LET,str.toString()));
        }else if (str.toString().equals("function")) {
            tokens.add(symbol(JParserSym.FUNCTION,str.toString()));
        }else if (str.toString().equals("in")) {
            tokens.add(symbol(JParserSym.IN,str.toString()));
        }else if (str.toString().equals("not")) {
            tokens.add(symbol(JParserSym.NOT,str.toString()));
        }else if (str.toString().equals("halt")) {
            tokens.add(symbol(JParserSym.HALT,str.toString()));
        }else if (str.toString().equals("read")) {
            tokens.add(symbol(JParserSym.READ,str.toString()));
        }else if (str.toString().equals("write")) {
            tokens.add(symbol(JParserSym.WRITE,str.toString()));
        }else if (str.toString().equals("else")) {
            tokens.add(symbol(JParserSym.ELSE,str.toString()));
        }else if (str.toString().equals("if")) {
            tokens.add(symbol(JParserSym.IF,str.toString()));
        }else if (str.toString().equals("while")) {
            tokens.add(symbol(JParserSym.WHILE,str.toString()));
        } else {
            out = false;
        }

        return out;
    }


    //-----------------------------KeySymbols-------------------------------------

    private boolean isKeySymbol(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("(")) {
            tokens.add(symbol(JParserSym.BRACE_OPEN,str.toString()));
        }else if (str.toString().equals(")")) {
            tokens.add(symbol(JParserSym.BRACE_CLOSE,str.toString()));
        }else if (str.toString().equals("}")) {
            tokens.add(symbol(JParserSym.CURLY_BRACE_CLOSE,str.toString()));
        }else if (str.toString().equals("{")) {
            tokens.add(symbol(JParserSym.CURLY_BRACE_OPEN,str.toString()));
        }else if (str.toString().equals("=")) {
            tokens.add(symbol(JParserSym.EQ,str.toString()));
        }else if (str.toString().equals("<-")) {
            tokens.add(symbol(JParserSym.TO,str.toString()));
        }else if (str.toString().equals(",")) {
            tokens.add(symbol(JParserSym.COMMA,str.toString()));
        }else if (str.toString().equals(":")) {
            tokens.add(symbol(JParserSym.COLON,str.toString()));
        }else if (str.toString().equals(";")) {
            tokens.add(symbol(JParserSym.SEMICOLON,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------AdditiveOp-------------------------------------

    private boolean isAdditiveOp(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("+")) {
            tokens.add(symbol(JParserSym.PLUS,str.toString()));
        }else if (str.toString().equals("-")) {
            tokens.add(symbol(JParserSym.MINUS,str.toString()));
        }else if (str.toString().equals("or")) {
            tokens.add(symbol(JParserSym.OR,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------RelationalOp-------------------------------------

    private boolean isRelationalOp(StringBuffer str) {
        boolean out = true;

        if (str.toString().matches("^(<|>|==|!=|<=|>=)")) {
            tokens.add(symbol(JParserSym.RELATIONAL_OP,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------MultiplicativeOp-------------------------------------

    private boolean isMultiplicativeOp(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("*")) {
            tokens.add(symbol(JParserSym.MULTIPLICATION,str.toString()));
        }else if (str.toString().equals("/")) {
            tokens.add(symbol(JParserSym.DIVISION,str.toString()));
        }else if (str.toString().equals("and")) {
            tokens.add(symbol(JParserSym.AND,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------Identifier-------------------------------------

    private boolean isIdentifier(StringBuffer str) {
        boolean out = true;

        if (str.toString().matches("^[_A-Za-z][_A-Za-z0-9]*")) {
            tokens.add(symbol(JParserSym.IDENTIFIER,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------Type-------------------------------------

    private boolean isType(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("int")) {
            tokens.add(symbol(JParserSym.TYPE,str.toString()));
        } else if (str.toString().equals("bool")) {
            tokens.add(symbol(JParserSym.TYPE,str.toString()));
        } else if (str.toString().equals("char")) {
            tokens.add(symbol(JParserSym.TYPE,str.toString()));
        } else if (str.toString().equals("real")) {
            tokens.add(symbol(JParserSym.TYPE,str.toString()));
        } else if (str.toString().equals("string")) {
            tokens.add(symbol(JParserSym.TYPE,str.toString()));
        } else if (str.toString().equals("unit")) {
            tokens.add(symbol(JParserSym.TYPE,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------BooleanLiteral-------------------------------------

    private boolean isBooleanLiteral(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("true")) {
            tokens.add(symbol(JParserSym.BOOLEAN_LITERAL,new Boolean(true)));
        } else if (str.toString().equals("false")) {
            tokens.add(symbol(JParserSym.BOOLEAN_LITERAL,new Boolean(true)));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------IntegerLiteral-------------------------------------

    private boolean isIntegerLiteral(StringBuffer str) {
        boolean out = true;

        if (str.toString().matches("^[0-9]+$")) {
            tokens.add(symbol(JParserSym.INTEGER_LITERAL,new Integer(str.toString())));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------RealLiteral-------------------------------------

    private boolean isRealLiteral(StringBuffer str) {
        boolean out = true;

        if (str.toString().matches("^[0-9]+\\.[0-9]+([eE][-+]?[0-9]+)?$")) {
            tokens.add(symbol(JParserSym.REAL_LITERAL,new Double(str.toString())));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------CharLiteral-------------------------------------

    private boolean isCharLiteral(StringBuffer str) {
        boolean out = true;

        if (str.toString().matches("'[\\x20-\\x7E]'")) {
            tokens.add(symbol(JParserSym.CHAR_LITERAL,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------StringLiteral-------------------------------------

    private boolean isStringLiteral(StringBuffer str) {
        boolean out = true;

        if (str.toString().matches("\"[\\x20-\\x7E]+\"")) {
            tokens.add(symbol(JParserSym.STRING_LITERAL,str.toString()));
        } else {
            out = false;
        }

        return out;
    }

    //-----------------------------UnitLiteral-------------------------------------

    private boolean isUnitLiteral(StringBuffer str) {
        boolean out = true;

        if (str.toString().equals("#")) {
            tokens.add(symbol(JParserSym.UNIT_LITERAL));
        } else {
            out = false;
        }

        return out;
    }


    private ArrayList<Symbol> lexIt() throws IOException {
        tokens = new ArrayList<Symbol>();
        errorLog = new ArrayList<Object[]>();
        done = false;
        lineNumber = 1;
        readerLineNumber = 1;

        lineNumber = readerLineNumber;
        String startBuffer = getWord().toString();
        String endBuffer = "";

        while (startBuffer.length() != 0) {
            if (!process(startBuffer)) {
                if (startBuffer.length() == 1) {
                    errorLogger(startBuffer, lineNumber);
                    startBuffer = "";
                } else {
                    endBuffer = startBuffer.charAt(startBuffer.length() - 1) + endBuffer;
                    startBuffer = startBuffer.substring(0, startBuffer.length() - 1);
                }
            } else {
                startBuffer = "";
            }

            if (startBuffer.length() == 0) {
                if (endBuffer.length() == 0) {
                    lineNumber = readerLineNumber;
                    startBuffer = getWord().toString();
                } else {
                    startBuffer = endBuffer;
                    endBuffer = "";
                }
            }
        }

        dumpErrors();

        return tokens;
    }

    private boolean process(String strIn) {
        StringBuffer str = new StringBuffer(strIn);

        boolean out = isType(str) || isBooleanLiteral(str) || isIntegerLiteral(str);
        out = out || isRealLiteral(str) || isStringLiteral(str) || isCharLiteral(str);
        out = out || isUnitLiteral(str) || isMultiplicativeOp(str) || isRelationalOp(str);
        out = out || isAdditiveOp(str) || isKeySymbol(str) || isKeyword(str);
        out = out || isIdentifier(str);
        return out;
    }

    private StringBuilder getWord() throws IOException {
        if (done) return new StringBuilder("");

        boolean openQuotes = false;
        char quote = '\'';

        StringBuilder out = new StringBuilder();

        int c = dumpWhiteSpace();

        while (c != EOF) {

            if (c == '/' && !openQuotes) {
                c = bufferToRead.read();
                if (c == '/') {
                    c = dumpUntilNewLine();
                } else {
                    out.append("/");
                }
            }


            if ((!openQuotes && c == '\'' || c == '"') || (openQuotes && c == quote)) {
                quote = (char) c;
                if (out.length() == 0 || out.toString().charAt(out.toString().length() - 1) != '\\') {
                    openQuotes = !openQuotes;
                }
            }

            if ((isWhiteSpace((char) c) && !openQuotes)) {
                return out;
            } else if (openQuotes && isNewLine((char) c)) {
                //tokens.add(new Token(TokenClass.ERROR, "Missing close quotes with string " + out.toString(), lineNumber));
                errorLogger("Missing close quotes with string " + out.toString(), lineNumber);
                openQuotes = false;

                c = dumpWhiteSpace();
                out.setLength(0);

                if (c == EOF) {
                    return out;
                }

            }

            if (!isNewLine((char) c)) {
                out.append((char) c);
            }

            c = bufferToRead.read();
        }

        done = true;

        return out;
    }

    private int dumpWhiteSpace() throws IOException {
        int c = (char) bufferToRead.read();

        while (c != EOF && isWhiteSpace((char) c)) {
            c = (char) bufferToRead.read();
        }

        return c;
    }

    //comment system
    private int dumpUntilNewLine() throws IOException {
        int c = (char) bufferToRead.read();
        while (c != EOF && !isNewLine((char) c)) {
            c = (char) bufferToRead.read();
        }

        readerLineNumber++;

        return dumpWhiteSpace();
    }


    private boolean isNewLine(char c) {
        return (c == '\n');
    }

    private boolean isWhiteSpace(char c) {
        if (isNewLine(c)) readerLineNumber++;
        return Character.isWhitespace(c);
    }

    public int errorsCount() {
        return errorLog.size();
    }

    private void errorLogger (String str,int lineNumber){
        Object[] obj = {str, new Integer((lineNumber))};
        errorLog.add(obj);
    }

    private void dumpErrors() {
        for (int loops = 0; loops < errorLog.size(); loops++) {
            System.out.println("Unexpected char ' " + errorLog.get(loops)[0].toString() + " ' " + (useLineNumbers? "on line "  + errorLog.get(loops)[1].toString()  : "") );
        }
    }

}