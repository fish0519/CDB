package org;

import org.client.DbClient;
import org.parser.Calc;
import org.parser.CalcLexer;
import org.util.MyMapFile;

import java.io.*;
import java.net.URLDecoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static org.parser.Calc.YYRETURN;

public class MyTest {
    public static void main(String[] args) throws IOException {

        String sql = "(1+2)*3/2\n";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(sql.getBytes());
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(byteArrayInputStream));

        CalcLexer scanner = new CalcLexer(byteArrayInputStream);
        Calc parser = new Calc(scanner);
        for (String arg : args)
            if (arg.equals("-p"))
                parser.setDebugLevel(1);
        int status;
        do {
            int token = scanner.getToken();
            Object lval = scanner.getValue();
            Calc.Location yyloc = scanner.getLocation();
            status = parser.push_parse(token, lval, yyloc);
            if(status == YYRETURN)
            {
                Object object = scanner.getValue();
                System.out.println(object);
            }

        } while (status == Calc.YYPUSH_MORE);
        if (status != Calc.YYACCEPT)
            System.exit(1);

    }
}
