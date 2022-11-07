package org.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyMapFile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FileClientHandler extends ChannelInboundHandlerAdapter {

    MyMapFile readFile;
    MyMapFile writeFile;
    public static int num = 0;
    public static ExecutorService threadPool = Executors.newSingleThreadExecutor();

    public FileClientHandler(MyMapFile readFile, MyMapFile writeFile) {
       this.readFile = readFile;
       this.writeFile = writeFile;
    }
    public void channelActive(ChannelHandlerContext ctx) {
        ByteBuf byteBuf = readFile.readFile();
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
        System.out.print("第"+ (++num) +"条服务端消息:" + serverMsg);
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                byte[] sBytes = serverMsg.getBytes();
                writeFile.writeFile(sBytes);
            }
        });

        ByteBuf byteBuf = readFile.readFile();
        if(byteBuf != null)
        {
            ctx.writeAndFlush(byteBuf);
        }else {
            System.out.println("文件已经全部传输, 等待回写结果");
            threadPool.shutdown();
            while(!threadPool.awaitTermination(10, TimeUnit.MILLISECONDS))
            {
                System.out.println("writing");
            }
            System.out.println("Success");
            ctx.close();
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
