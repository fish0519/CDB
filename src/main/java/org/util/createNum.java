package org.util;

import java.io.*;
import java.util.Properties;
import java.util.Random;

public class createNum {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        String homePath = new File(System.getProperty("user.dir")).getAbsolutePath();
        properties.load(new InputStreamReader(new FileInputStream(new File(homePath, "conf/client.properties"))));
        String readFileName = properties.getProperty("srcFile").trim();
        File readFile = new File(readFileName);

        StringBuffer sb = new StringBuffer();
        for(int i=0; i<1000; i++)
        {
            Random random1 = new Random();
            Random random2 = new Random();
            int col = random1.nextInt(984) + 16;
            for(int j=0; j<col; j++)
            {
                sb.append(random2.nextInt(1000));
                if(j != col - 1)
                    sb.append(",");
            }
            sb.append("\r\n");
        }

        FileWriter fileWriter = new FileWriter(readFile);
        fileWriter.write(sb.toString());
        fileWriter.close();
    }
}
