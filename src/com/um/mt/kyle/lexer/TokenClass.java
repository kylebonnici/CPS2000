package com.um.mt.kyle.lexer;

public enum TokenClass {
    TYPE,
    SIMPLE_EXPRESSION,
    TERM,
    FACTOR,
    BLOCK,
    FORMAL_PARAM,
    EXPRESSION,
    STATEMENT,
    BOOLEAN_LITERAL,
    INTEGER_LITERAL,
    REAL_LITERAL,
    CHAR_LITERAL,
    STRING_LITERAL,
    UNIT_LITERAL,
    LITERAL,
    IDENTIFIER,
    MULTIPLICATIVE_OP,
    ADDITIVE_OP,
    RELATIONAL_OP,
    KEY_SYMBOL,
    KEYWORD,
    ERROR
}