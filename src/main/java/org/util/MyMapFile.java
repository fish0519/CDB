package org.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

//单线程操作
public class MyMapFile {

    public long size;
    public int mapSize;
    public RandomAccessFile raf;
    public FileChannel fileChannel;
    public long index = 0;

    public MyMapFile(File file, int mapSize, String mode) throws FileNotFoundException {
        this.raf = new RandomAccessFile(file, mode);
        this.fileChannel = raf.getChannel();
        this.mapSize = mapSize;
        this.size = file.length();
    }

    public synchronized ByteBuf readFile()
    {
        long curIndex = index;
        System.out.println("readFile");
        if(curIndex < size)
        {
            long targetIndex = (curIndex + mapSize) > size ? size : curIndex + mapSize;
            int initialCapacity = (int)(targetIndex - curIndex + 1);

            byte[] byteArr = new byte[initialCapacity + 1];

            int byteArrLineIndex = 0;

            try {
                System.out.println("读取" + curIndex + "到" + targetIndex + "成功");
                MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, curIndex, targetIndex - curIndex);

                int i = 0;
                for(; i < targetIndex - curIndex; i++)
                {
                    byte bb = map.get();
                    if(bb == 10) //换行符
                    {
                        byteArrLineIndex = i;
                    }
                    byteArr[i] = bb;
                }

                if(byteArrLineIndex == 0)//最后一行,没有换行符
                {
                    byteArrLineIndex = i - 1;
                }
                index = index + byteArrLineIndex + 1;

            } catch (Exception e) {
                e.printStackTrace();
            }

            Scanner scan = new Scanner(new ByteArrayInputStream(byteArr, 0, byteArrLineIndex + 1)).useDelimiter("\\R");
            while (scan.hasNext()) {
                //测试一下读取的内容
                System.out.println(Thread.currentThread().getName() + "===>" + scan.next() + " ");
            }
            scan.close();

            byteArr[byteArrLineIndex + 1] = '$';
            ByteBuf byteBuf = Unpooled.copiedBuffer(byteArr, 0, byteArrLineIndex + 2);
            return byteBuf;
        }
        return null;
    }

    public synchronized void writeFile(byte[] byteArr)
    {
        long curIndex = index;
        System.out.println("writeFile");

        long targetIndex = curIndex + byteArr.length;
        try {
            System.out.println("写" + curIndex + "到" + targetIndex + "成功");
            System.out.println(new String(byteArr));
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, curIndex, targetIndex - curIndex);
            map.position();
            map.put(byteArr);
        }catch (IOException e) {
            e.printStackTrace();
        }
        index = targetIndex;
    }

}
