package org.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledDirectByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyMapFile;

import java.nio.charset.StandardCharsets;

public class FileClientHandler extends ChannelInboundHandlerAdapter {

    MyMapFile readFile;
    MyMapFile writeFile;
    public static int num = 0;

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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        String serverMsg = (String)msg;
        System.out.println("第"+ (++num) +"条服务端消息:" + serverMsg);

        ByteBuf byteBuf = readFile.readFile();
        if(byteBuf != null)
        {
            ctx.writeAndFlush(byteBuf);
        }else {
            System.out.println("文件已经读完");
        }
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
