package org.util;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

//单线程操作
public class MyMapFile {

    public long size;
    public int mapSize;
    public RandomAccessFile raf;
    public FileChannel fileChannel;
    public AtomicInteger index = new AtomicInteger(0);

    public MyMapFile(File file, int mapSize, String mode) throws FileNotFoundException {
        this.raf = new RandomAccessFile(file, mode);
        this.fileChannel = raf.getChannel();
        this.mapSize = mapSize;
        this.size = file.length();
    }

    public byte[] readFile()
    {
        int curIndex = index.get();
        System.out.println("readFile");
        if(curIndex < size)
        {
            int targetIndex = (curIndex + mapSize) > size ? (int)size : curIndex + mapSize;
            if(index.compareAndSet(curIndex, targetIndex + 1))
            {
                byte[] byteArr = new byte[targetIndex - curIndex];
                try {
                    System.out.println("读取" + curIndex + "到" + targetIndex + "成功");
                    MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, curIndex, targetIndex - curIndex);
                    map.get(byteArr, 0, targetIndex - curIndex);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Scanner scan = new Scanner(new ByteArrayInputStream(byteArr)).useDelimiter("\\R");
                while (scan.hasNext()) {
                    //测试一下读取的内容
                    System.out.println(Thread.currentThread().getName() + "===>" + scan.next() + " ");
                }
                scan.close();
                return byteArr;
            }
        }
        return null;
    }

    public void writeFile(byte[] byteArr)
    {
        int curIndex = index.get();
        System.out.println("writeFile");

        int targetIndex = curIndex + byteArr.length;
        if(index.compareAndSet(curIndex, targetIndex + 1))
        {
            try {
                System.out.println("写" + curIndex + "到" + targetIndex + "成功");
                MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, curIndex, targetIndex - curIndex);
                map.position();
                map.put(byteArr);
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
