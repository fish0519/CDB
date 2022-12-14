package org.util;

import org.client.DbClient;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Random;

public class createNum {
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        String homePath = new File(System.getProperty("user.dir")).getAbsolutePath();
        properties.load(new InputStreamReader(new FileInputStream(new File(homePath, "conf/client.properties"))));
        String readFileName = properties.getProperty("srcFile").trim();
        if(readFileName.length() == 0)
        {
            readFileName = DbClient.class.getResource("/1.txt").getPath();
            readFileName = URLDecoder.decode(readFileName, "UTF-8");
        }
        File readFile = new File(readFileName);
        FileWriter fileWriter = new FileWriter(readFile);


        for(int i=0; i<400000; i++)
        {
            StringBuffer sb = new StringBuffer();
            Random random1 = new Random();
            Random random2 = new Random();
            int col = random1.nextInt(984) + 16;
            for(int j=0; j<col; j++)
            {
                sb.append(random2.nextInt(100000));
                if(j != col - 1)
                    sb.append(",");
            }
            sb.append("\r\n");
            if(i == 0)
            {
                fileWriter.write(sb.toString());
            }else{
                fileWriter.append(sb);
            }
        }

        fileWriter.close();
    }
}
