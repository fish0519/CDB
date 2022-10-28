package org;

import org.client.DbClient;
import org.util.MyMapFile;

import java.io.*;
import java.net.URLDecoder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MyTest {
    public static void main(String[] args) {

        try {
            String writeFileName = DbClient.class.getResource("/2.txt").getPath();
            writeFileName = URLDecoder.decode(writeFileName, "UTF-8");

            File writeFile = new File(writeFileName);
            RandomAccessFile raf = new RandomAccessFile(writeFileName, "rw");


            int count = 10;
//            RandomAccessFile memoryMappedFile = new RandomAccessFile("D:\\1.txt", "rw");

            System.out.println("writeFile");

            byte[] byteArr = "hello".getBytes();

            try {

                MappedByteBuffer out = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, count);

                for (int i = 0; i < count; i++) {
                    out.put((byte) i);
                }
                out.force();
                raf.close();

//                System.out.println(new String(byteArr));
//                MappedByteBuffer map = raf.getChannel().map(FileChannel.MapMode.READ_WRITE, 0, byteArr.length);
////                map.position();
//                map.put(byteArr);

            }catch (IOException e) {
                e.printStackTrace();
            }

        } catch (UnsupportedEncodingException | FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
