package org.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyMapFile;

public class SeqFileClientHandler extends ChannelInboundHandlerAdapter {

    MyMapFile readFile;
    MyMapFile writeFile;
    int seq;
    public static int num = 0;

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
                System.out.println("文件已经读完");
            }
        }finally {
            readFile.readLock.unlock();
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {

        final String serverMsg = (String)msg;
        System.out.print("第"+ (++num) +"条服务端消息:" + serverMsg);

        while(true)
        {
            if(seq == writeFile.writeSeq.get())
            {
                try {
                    writeFile.writeLock.lock();

                    writeFile.writeSeq.incrementAndGet();
                    byte[] sBytes = serverMsg.getBytes();
                    writeFile.writeFile(sBytes);

                    break;
                }finally {
                    writeFile.writeLock.unlock();
                }
            }else{
                Thread.sleep(5);
            }
        }

        try {
            readFile.readLock.lock();
            seq = readFile.readSeq.getAndIncrement();
            ByteBuf byteBuf = readFile.readFile();
            if(byteBuf != null)
            {
                ctx.writeAndFlush(byteBuf);
            }else {
                System.out.println("文件已经读完");
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

