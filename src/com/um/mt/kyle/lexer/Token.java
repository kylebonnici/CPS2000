package com.um.mt.kyle.lexer;

import java.util.Objects;

public class Token {
    private TokenClass clazz;
    private Object lexeme;
    private int lineNumber;

    public Token(TokenClass clazz, Object lexeme, int lineNumber) {
        this.clazz = clazz;
        this.lexeme = lexeme;
        this.lineNumber = lineNumber;
    }

    public TokenClass getClazz() {
        return clazz;
    }

    public Object getLexeme() {
        return lexeme;
    }

    public int getLineNumber(){
        return lineNumber;
    }


    public Token getDeepestToken(){
        if (lexeme instanceof Token){
            return (((Token) lexeme).getDeepestToken());
        }

        return this;
    }

    public boolean isTokenClazz(TokenClass clazz){
        boolean out = this.clazz == clazz;

        if (out) return out;

        if (lexeme instanceof Token){
            out = ((Token) lexeme).isTokenClazz(clazz);
        }

        return out;
    }

    @Override
    public String toString() {
        String strLexeme = "";
        if (lexeme == null){
            //strLexeme = ", lexeme='null'";
        }else {
            strLexeme = (!lexeme.toString().equals("")? ", lexeme='" + lexeme.toString() + '\'': "");
        }

        return "Token{" +
                "clazz=" + clazz +
                strLexeme +
                ", lineNumber= " + lineNumber + " }";
    }
}
