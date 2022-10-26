package org.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyMapFile;

public class FileClientHandler extends ChannelInboundHandlerAdapter {

    MyMapFile readFile;
    MyMapFile writeFile;

    public FileClientHandler(MyMapFile readFile, MyMapFile writeFile) {
       this.readFile = readFile;
       this.writeFile = writeFile;
    }
    public void channelActive(ChannelHandlerContext ctx) {
        byte[] bytes = readFile.readFile();
        if(bytes.length > 0)
        {
            ctx.writeAndFlush(bytes);
        }else {
            System.out.println("文件已经读完");
        }
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String serverMsg = (String)msg;
        System.out.println("服务端消息:" + serverMsg);

        byte[] bytes = readFile.readFile();
        if(bytes.length > 0)
        {
            ctx.writeAndFlush(bytes);
        }else {
            System.out.println("文件已经读完");
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
