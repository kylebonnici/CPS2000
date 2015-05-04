package mt.edu.um.cs.cps2000.runtime;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import mt.edu.um.cs.cps2000.parseandlexer.JFlexLexer;
import mt.edu.um.cs.cps2000.parseandlexer.JParser;
import mt.edu.um.cs.cps2000.parseandlexer.Lexer;
import mt.edu.um.cs.cps2000.parseandlexer.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by kylebonnici on 01/05/15.
 */
public class Runtime extends Execute{
    private Document doc;
    private boolean useMyLexer = true;
    private boolean useMyParser = false;

    public Runtime(){
        start();
    }

    private Scanner getLexer(BufferedReader br) throws IOException{
        if (useMyLexer) {
                Lexer lexer = new Lexer(br);
                lexer.useLineNumbers = showLineNumbers;
                return lexer;
        }
        else return new JFlexLexer(br);
    }



    private void start(){
        String in;

        while(true) {
            boolean error = false;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(inStream));
                System.out.print("sxl>");
                String s = br.readLine();
                Scanner lexer = null;
                BufferedReader brLex = null;
                boolean load = s.startsWith("load \"");
                if (load) {
                    String fileToLoad = s.substring("load \"".length(), s.length() - 1);

                    try {
                        brLex = new BufferedReader(new FileReader(fileToLoad));
                        showLineNumbers = true;
                        lexer = getLexer(brLex);
                    } catch (IOException e) {
                        System.out.println("Error reading file! " + e.getMessage());
                    }
                } else {
                    // convert String into InputStream
                    InputStream is = new ByteArrayInputStream(s.getBytes());
                    showLineNumbers = false;
                    // read it with BufferedReader
                    brLex = new BufferedReader(new InputStreamReader(is));
                    lexer = getLexer(brLex);
                }

                if (lexer!=null) {

                    DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder icBuilder;

                    try {
                        icBuilder = icFactory.newDocumentBuilder();
                        doc = icBuilder.newDocument();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Parse
                    if (useMyParser){
                        Parser parser = new Parser(lexer);
                        parser.useLineNumbers = showLineNumbers;
                        parser.setDoc(doc);
                        parser.parse();
                        error = parser.getError();
                    }else {
                        JParser parser = new JParser(lexer);
                        parser.setDoc(doc);
                        try {
                            Symbol sym  = parser.parse();
                            doc.appendChild((Node) sym.value);
                        }catch (Exception e){
                            error = true;
                        }

                    }

                    if (!error) run(doc.getDocumentElement());

                    try {
                        brLex.close();
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(new File("/Users/kylebonnici/IdeaProjects/CPS2000/src/resources/" + (useMyParser? "mine.xml" : "jflex.xml" )));
                        transformer.transform(source, result);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } catch (IOException ex) {
                System.out.println("System Error:" + ex.toString());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

    }
}
