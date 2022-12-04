package org.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyMapFile;

import java.util.concurrent.atomic.AtomicInteger;

public class MultiFileClientHandler extends ChannelInboundHandlerAdapter {

    MyMapFile readFile;
    MyMapFile writeFile;
    public static AtomicInteger num = new AtomicInteger(0);

    public MultiFileClientHandler(MyMapFile readFile, MyMapFile writeFile) {
        this.readFile = readFile;
        this.writeFile = writeFile;
    }
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = null;
        while(byteBuf == null && MyMapFile.finishRead.get() == false)
        {
            byteBuf = readFile.read();
        }
        if(byteBuf != null)
        {
            ctx.writeAndFlush(byteBuf);
        }else {
            System.out.println("文件已经读完");
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, final Object msg) throws Exception {

        final String serverMsg = (String)msg;
        int msgNum = num.incrementAndGet();
        System.out.println();
        System.out.println("第"+ msgNum +"条服务端消息:" + serverMsg);
        byte[] sBytes = serverMsg.getBytes();
        boolean writeFlag = writeFile.write(sBytes);
        while (!writeFlag)
        {
            writeFlag = writeFile.write(sBytes);
        }

        ByteBuf byteBuf = null;
        while(byteBuf == null && MyMapFile.finishRead.get() == false)
        {
            byteBuf = readFile.read();
        }
        if(byteBuf != null)
        {
            ctx.writeAndFlush(byteBuf);
        }else {
            System.out.println(Thread.currentThread().getId() + ": Success");
            ctx.close();
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
