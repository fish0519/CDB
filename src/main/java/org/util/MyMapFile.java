package org.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//单线程操作
public class MyMapFile {

    public long size;
    public int mapSize;
    public RandomAccessFile raf;
    public FileChannel fileChannel;
    public long index = 0;
    public File file;

    //方案1:用数组控制并发
    public static int threadNum;
    public static int count = 0;
    static {
        threadNum = Integer.parseInt(System.getProperty("clientNum"));
        threadArr = new int[threadNum];
    }
    public static int[] threadArr; //0 可读；1 可写
    public static boolean flag = true;// true 可读；false 可写

    public static AtomicBoolean finishRead = new AtomicBoolean(false);

    //方案2:用锁并发控制
    public AtomicInteger readSeq = new AtomicInteger(0);
    public AtomicInteger writeSeq = new AtomicInteger(0);
    public ReentrantLock readLock = new ReentrantLock();
    public ReentrantLock writeLock = new ReentrantLock();

    public MyMapFile(File file, int mapSize, String mode) throws FileNotFoundException {
        this.raf = new RandomAccessFile(file, mode);
        this.fileChannel = raf.getChannel();
        this.mapSize = mapSize;
        this.size = file.length();
        this.file = file;
    }

    public ByteBuf readFile()
    {
        long curIndex = index;
        if(curIndex < size)
        {
            long targetIndex = (curIndex + mapSize) > size ? size : curIndex + mapSize;
            int initialCapacity = (int)(targetIndex - curIndex + 1);

            byte[] byteArr = new byte[initialCapacity + 1];

            int byteArrLineIndex = 0;

            try {
//                System.out.println("读取" + curIndex + "到" + targetIndex + "成功");
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

//            Scanner scan = new Scanner(new ByteArrayInputStream(byteArr, 0, byteArrLineIndex + 1)).useDelimiter("\\R");
//            while (scan.hasNext()) {
//                //测试一下读取的内容
//                System.out.println(Thread.currentThread().getName() + "===>" + scan.next() + " ");
//            }
//            scan.close();

            byteArr[byteArrLineIndex + 1] = '$';
//            ByteBuf byteBuf = Unpooled.copiedBuffer(byteArr, 0, byteArrLineIndex + 2);
            //使用堆外内存
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(byteArrLineIndex + 2);
            byteBuf.writeBytes(byteArr, 0, byteArrLineIndex + 2);
            return byteBuf;
        }
        return null;
    }

    public void writeFile(byte[] byteArr)
    {
        long curIndex = index;
//        System.out.println("writeFile");

        long targetIndex = curIndex + byteArr.length;
        try {
//            System.out.println("写" + curIndex + "到" + targetIndex + "成功");
//            System.out.println(new String(byteArr));
            MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, curIndex, targetIndex - curIndex);
            map.position();
            map.put(byteArr);
        }catch (IOException e) {
            e.printStackTrace();
        }
        index = targetIndex;
    }

    public void closeFile()
    {
        try {
            if(fileChannel != null)
            {
                fileChannel.close();
            }

            if(raf != null)
            {
                raf.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 成功返回非空值；失败返回空(包括不允许读和已读完)
    public ByteBuf read()
    {
        int threadId = (int) (Thread.currentThread().getId() % threadNum);
        if(flag
            && threadArr[threadId] == 0
            && threadId == count)
        {
            synchronized (this)
            {
                threadArr[threadId] = 1;
                count ++;

                if(count == threadNum)
                {
                    flag = false;
                    count = 0;
                }
                ByteBuf byteBuf = readFile();
                if(byteBuf == null)
                {
                    finishRead.set(true);
                }
                System.out.println("第" + threadId + "号线程读取文件成功" );
                return byteBuf;
            }
        }else
        {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    //写成功返回true;失败返回false(不允许写)
    public boolean write(byte[] byteArr)
    {
        int threadId = (int) (Thread.currentThread().getId() % threadNum);
        if(!flag
                && threadArr[threadId] == 1
                && threadId == count)
        {
            synchronized (this)
            {
                writeFile(byteArr);
                threadArr[threadId] = 0;
                count ++;
                if(count == threadNum)
                {
                    count = 0;
                    flag = true;
                }
                System.out.println("第" + threadId + "号线程写文件成功" );
                return true;
            }
        }else
        {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }
    }

}
