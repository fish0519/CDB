package org.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.util.MyFile;

import java.io.File;
import java.io.RandomAccessFile;

public class FileServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String file_dir = "D:";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String clientMsg = (String)msg;
        System.out.println("客户端消息:"+clientMsg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
