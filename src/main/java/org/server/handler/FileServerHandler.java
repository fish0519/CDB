package org.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class FileServerHandler extends ChannelInboundHandlerAdapter {

    public static int num = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String clientMsg = (String)msg;
        System.out.println("第"+(++num)+"条客户端消息:"+clientMsg);

        String serverMsg = "服务端的"+num+"$";
        ByteBuf resp = Unpooled.copiedBuffer(serverMsg.getBytes());
        ctx.writeAndFlush(resp);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
