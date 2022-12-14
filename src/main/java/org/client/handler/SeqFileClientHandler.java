package org.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyMapFile;

import java.util.concurrent.atomic.AtomicInteger;

public class SeqFileClientHandler extends ChannelInboundHandlerAdapter {

    MyMapFile readFile;
    MyMapFile writeFile;
    int seq;
    public static AtomicInteger num = new AtomicInteger(0);

    public static AtomicInteger finishNum = new AtomicInteger(0);
    public static long startTime = System.currentTimeMillis();

    public SeqFileClientHandler(MyMapFile readFile, MyMapFile writeFile) {
        this.readFile = readFile;
        this.writeFile = writeFile;
    }
    public void channelActive(ChannelHandlerContext ctx) {
        try {
            readFile.readLock.lock();

            seq = readFile.readSeq.getAndIncrement();
            ByteBuf byteBuf = readFile.readFile();
            if(byteBuf != null)
            {
                ctx.writeAndFlush(byteBuf);
            }else {
                int finish = finishNum.incrementAndGet();
                if(finish == Integer.parseInt(System.getProperty("clientNum")))
                {
                    readFile.finishRead.set(true);
                    long endTime = System.currentTimeMillis();
                    System.out.println("总耗时:"+(endTime-startTime)/1000);
                }
                System.out.println("文件已经读完");
            }
        }finally {
            readFile.readLock.unlock();
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {

        final String serverMsg = (String)msg;
        int serverNum = num.getAndIncrement();
        if(serverNum%300 == 0)
        {
            System.out.println(Thread.currentThread().getId()+"接收第"+ serverNum +"条服务端消息:");
        }

        // 方式1:本线程写文件
//        while(true)
//        {
//            if(seq == writeFile.writeSeq.get())
//            {
//                try {
//                    writeFile.writeLock.lock();
//
//                    writeFile.writeSeq.incrementAndGet();
//                    byte[] sBytes = serverMsg.getBytes();
//                    writeFile.writeFile(sBytes);
//
//                    break;
//                }finally {
//                    writeFile.writeLock.unlock();
//                }
//            }else{
//                Thread.sleep(2);
//            }
//        }

        //方式2:交给其它线程写文件
        writeFile.result[seq] = serverMsg.getBytes();
        writeFile.resultNum.getAndIncrement();

        try {
            readFile.readLock.lock();
            seq = readFile.readSeq.getAndIncrement();
            ByteBuf byteBuf = readFile.readFile();
            if(byteBuf != null)
            {
                ctx.writeAndFlush(byteBuf);
            }else {
                System.out.println("文件已经读完");
                int finish = finishNum.incrementAndGet();
                if(finish == Integer.parseInt(System.getProperty("clientNum")))
                {
                    readFile.finishRead.set(true);
                    long endTime = System.currentTimeMillis();
                    System.out.println("总耗时:"+(endTime-startTime)/1000);
                }
                ctx.close();
            }
        }finally {
            readFile.readLock.unlock();
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

