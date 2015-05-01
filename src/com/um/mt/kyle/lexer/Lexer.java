package com.um.mt.kyle.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class Lexer {
    private ArrayList<Token> tokens = new ArrayList<Token>();
    private ArrayList<String> errorLog;
    private boolean done = false;
    int lineNumber = 0;
    int readerLineNumber = 0;
    //private ArrayList<String> multiplicativeOp = new ArrayList<String>();
    private ArrayList<String> keySymbols = new ArrayList<String>();
    private ArrayList<String> keyWords = new ArrayList<String>();
    private BufferedReader bufferToRead;

    public Lexer(BufferedReader bufferToRead){
        this.bufferToRead = bufferToRead;

        //this.setMultiplicativeOp();
        this.setKeySymbols();
        this.setKeyWords();
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    private void setKeySymbols(){
        keySymbols.add("(");
        keySymbols.add(")");
        keySymbols.add(",");
        keySymbols.add("<-");
        keySymbols.add(";");
        keySymbols.add(":");
        keySymbols.add("{");
        keySymbols.add("}");
        keySymbols.add("=");
    }

    private void setKeyWords(){
        keyWords.add("not");
        keyWords.add("else");
        keyWords.add("set");
        keyWords.add("let");
        keyWords.add("if");
        keyWords.add("halt");
        keyWords.add("function");
        keyWords.add("while");
        keyWords.add("read");
        keyWords.add("write");
        keyWords.add("in");
    }

    /*
    private void setMultiplicativeOp(){
        multiplicativeOp.add("*");
        multiplicativeOp.add("/");
        multiplicativeOp.add("and");
    }
    */

    private static int EOF = -1;


    //-----------------------------Keyword-------------------------------------

    private boolean isKeyword(StringBuffer str){
        boolean out = true;

        if (keyWords.contains(str.toString())){
            tokens.add(new Token(TokenClass.KEYWORD, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }


    //-----------------------------KeySymbols-------------------------------------

    private boolean isKeySymbol(StringBuffer str){
        boolean out = true;

        if (keySymbols.contains(str.toString())){
            tokens.add(new Token(TokenClass.KEY_SYMBOL, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------AdditiveOp-------------------------------------

    private boolean isAdditiveOp(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("^(\\+|or|-)")){
            tokens.add(new Token(TokenClass.ADDITIVE_OP, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------RelationalOp-------------------------------------

    private boolean isRelationalOp(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("^(<|>|==|!=|<=|>=)")){
            tokens.add(new Token(TokenClass.RELATIONAL_OP, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------MultiplicativeOp-------------------------------------

    private boolean isMultiplicativeOp(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("^(\\*|and|/)")){
            tokens.add(new Token(TokenClass.MULTIPLICATIVE_OP, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------Identifier-------------------------------------

    private boolean isIdentifier(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("^[_A-Za-z][_A-Za-z0-9]*")){
            tokens.add(new Token(TokenClass.IDENTIFIER, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------Literal-------------------------------------

    private boolean isLiteral(int tokenIndex){
        Token token = tokens.get(tokenIndex);

        boolean isLiteral = token.getClazz() == TokenClass.CHAR_LITERAL || token.getClazz() == TokenClass.INTEGER_LITERAL;
        isLiteral = isLiteral || token.getClazz() == TokenClass.REAL_LITERAL || token.getClazz() == TokenClass.STRING_LITERAL;
        isLiteral = isLiteral || token.getClazz() == TokenClass.BOOLEAN_LITERAL || token.getClazz() == TokenClass.UNIT_LITERAL;

        if (isLiteral){
            tokens.remove(tokenIndex);
            tokens.add(tokenIndex,new Token(TokenClass.LITERAL,token,token.getLineNumber()));
        }

        return isLiteral;
    }

    //-----------------------------Type-------------------------------------

    private boolean isType(StringBuffer str){
        boolean out = true;

        if (str.toString().equals("int")){
            tokens.add(new Token(TokenClass.TYPE, str.toString(),lineNumber));
        }else if (str.toString().equals("bool")){
            tokens.add(new Token(TokenClass.TYPE,str.toString(),lineNumber));
        }else if (str.toString().equals("char")){
            tokens.add(new Token(TokenClass.TYPE,str.toString(),lineNumber));
        }else if (str.toString().equals("real")){
            tokens.add(new Token(TokenClass.TYPE,str.toString(),lineNumber));
        }else if (str.toString().equals("string")){
            tokens.add(new Token(TokenClass.TYPE,str.toString(),lineNumber));
        }else if (str.toString().equals("unit")){
            tokens.add(new Token(TokenClass.TYPE, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------BooleanLiteral-------------------------------------

    private boolean isBooleanLiteral(StringBuffer str){
        boolean out = true;

        if (str.toString().equals("true")){
            tokens.add(new Token(TokenClass.BOOLEAN_LITERAL,true,lineNumber));
        }else if (str.toString().equals("false")){
            tokens.add(new Token(TokenClass.BOOLEAN_LITERAL,false,lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------IntegerLiteral-------------------------------------

    private boolean isIntegerLiteral(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("^[0-9]+$")){
            tokens.add(new Token(TokenClass.INTEGER_LITERAL, Integer.parseInt(str.toString()),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------RealLiteral-------------------------------------

    private boolean isRealLiteral(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("^[0-9]+\\.[0-9]+([eE][-+]?[0-9]+)?$")){
            tokens.add(new Token(TokenClass.REAL_LITERAL, Double.parseDouble(str.toString()),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------CharLiteral-------------------------------------

    private boolean isCharLiteral(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("'[\\x20-\\x7E]'")){
            tokens.add(new Token(TokenClass.CHAR_LITERAL, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------StringLiteral-------------------------------------

    private boolean isStringLiteral(StringBuffer str){
        boolean out = true;

        if (str.toString().matches("\"[\\x20-\\x7E]+\"")){
            tokens.add(new Token(TokenClass.STRING_LITERAL, str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }

    //-----------------------------UnitLiteral-------------------------------------

    private boolean isUnitLiteral(StringBuffer str){
        boolean out = true;

        if (str.toString().equals("#")){
            tokens.add(new Token(TokenClass.UNIT_LITERAL,str.toString(),lineNumber));
        }else {
            out = false;
        }

        return out;
    }



    public ArrayList<Token> lexIt() throws IOException{
        tokens = new ArrayList<Token>();
        errorLog =  new ArrayList<String>();
        done = false;
        lineNumber = 1;
        readerLineNumber = 1;

        System.out.println("Starting lexer");

        lineNumber = readerLineNumber;
        String startBuffer = getWord().toString();
        String endBuffer = "";

        while (startBuffer.length() != 0){
            if (!process(startBuffer)){
                if (startBuffer.length() == 1){
                    tokens.add(new Token(TokenClass.ERROR, startBuffer, lineNumber));
                    startBuffer = "";
                }else{
                    endBuffer = startBuffer.charAt(startBuffer.length()-1) + endBuffer;
                    startBuffer = startBuffer.substring(0,startBuffer.length()-1);
                }
            }else {
                startBuffer = "";
            }

            if (startBuffer.length() == 0){
                if (endBuffer.length() == 0){
                    lineNumber = readerLineNumber;
                    startBuffer = getWord().toString();
                }else {
                    startBuffer = endBuffer;
                    endBuffer = "";
                }
            }
        }

        tokenShrink();

        produceMeaningFullErrors();

        dumpErrors();

        System.out.println("Lexer Done with " + errorsCount() + " Errors");

        return tokens;
    }

    private boolean process(String strIn){
        StringBuffer str = new StringBuffer(strIn);

        boolean out = isType(str) || isBooleanLiteral(str) || isIntegerLiteral(str);
        out = out || isRealLiteral(str) || isStringLiteral(str) || isCharLiteral(str);
        out = out || isUnitLiteral(str) || isMultiplicativeOp(str) || isRelationalOp(str);
        out = out || isAdditiveOp(str) || isKeySymbol(str) || isKeyword(str);
        out = out || isIdentifier(str);
        return out;
    }


    private void tokenShrink(){
        for (int loops = 0 ; loops < tokens.size(); loops++) {
            isLiteral(loops);
        }
    }

    private void produceMeaningFullErrors(){
        for (int loops = 0 ; loops < tokens.size(); loops++) {
            Token token = tokens.get(loops);

            if (token.getClazz() == TokenClass.ERROR ){
                errorLog.add("Error: line number " + token.getLineNumber() + " with ' " + token.getLexeme().toString() + " '");
            }
        }
    }


    private StringBuilder getWord() throws IOException{
        if (done) return new StringBuilder("");

        boolean openQuotes = false;
        char quote = '\'';

        StringBuilder out = new StringBuilder();

        int c = dumpWhiteSpace();

        while (c != EOF){

            if (c == '/' && !openQuotes){
                c = bufferToRead.read();
                if (c == '/'){
                    c = dumpUntilNewLine();
                }else{
                    out.append("/");
                }
            }


            if ((!openQuotes && c == '\'' || c == '"') || (openQuotes && c == quote)){
                quote = (char)c;
                if (out.length() == 0 || out.toString().charAt(out.toString().length()-1) != '\\') {
                    openQuotes = !openQuotes;
                }
            }

            if ( (isWhiteSpace((char)c) && !openQuotes)) {
                return out;
            }else if (openQuotes && isNewLine((char)c)){
                tokens.add(new Token(TokenClass.ERROR,"Missing close quotes with string " + out.toString(), lineNumber));
                openQuotes = false;

                c = dumpWhiteSpace();
                out.setLength(0);

                if (c == EOF){
                    return out;
                }

            }

            if (!isNewLine((char)c)) {
                out.append((char)c);
            }

            c = bufferToRead.read();
        }

        done = true;

       return  out;
    }

    private int dumpWhiteSpace() throws IOException{
        int c = (char)bufferToRead.read();

        while (c != EOF && isWhiteSpace((char)c)){
            c = (char)bufferToRead.read();
        }

        return c;
    }

    //comment system
    private int dumpUntilNewLine() throws IOException{
        int c = (char)bufferToRead.read();
        while (c != EOF && !isNewLine((char)c)){
            c = (char)bufferToRead.read();
        }

        readerLineNumber++;

        return dumpWhiteSpace();
    }


    private boolean isNewLine(char c){
        return (c == '\n');
    }

    private boolean isWhiteSpace(char c){
        if (isNewLine(c)) readerLineNumber ++;
        return Character.isWhitespace(c);
    }

    public int errorsCount(){
        return errorLog.size();
    }

    public boolean hasErrors(){
        return (errorsCount() != 0);
    }

    /*
    private boolean isLetter(char c){
        return Character.isLetter(c);
    }

    private boolean isDigit(char c){
        return Character.isDigit(c);
    }

    private boolean isPrintable(char c){
        return (c >= 0x20 && c <= 0x7E);
    }
    */


    public void displayTokens(ArrayList<Token> tokens){
        for (int loops = 0 ; loops < tokens.size(); loops ++){
            System.out.println(tokens.get(loops).toString());
        }
    }

    private void dumpErrors(){
        if (errorLog.size() > 0){
            System.out.println("Error log:");
        }
        for (int loops = 0 ; loops < errorLog.size(); loops ++){
            System.out.println(errorLog.get(loops));
        }
    }
}
