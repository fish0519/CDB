package org.example;

import org.parser.MyLexer;
import org.parser.Parser;

import java.io.IOException;
import java.io.StringReader;

public class ParserTest {
    public static void main(String[] args) {
        String sql = "1+2+3+4\n5+6\n";

        Parser.Lexer lexer = new MyLexer(new StringReader(sql));
        Parser parser = new Parser(lexer);

        StringBuilder result = new StringBuilder();

        try {
            if(parser.parse())
            {
                System.out.println(parser.result);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
