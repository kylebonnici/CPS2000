package com.um.mt.kyle.runner;

import com.um.mt.kyle.lexer.Lexer;
import com.um.mt.kyle.parser.Parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String args[]) {
        String inputPath = "/Users/kylebonnici/IdeaProjects/CPS2000/src/code.txt";

        BufferedReader br = null;

        Parser parser = new Parser(inputPath);
    }
}
