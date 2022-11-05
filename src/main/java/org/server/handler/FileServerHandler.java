package org.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.parser.MyLexer;
import org.parser.Parser;

import java.io.IOException;
import java.io.StringReader;

public class FileServerHandler extends ChannelInboundHandlerAdapter {

    public static int num = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String clientMsg = (String)msg;
        System.out.println("第"+(++num)+"条客户端消息:"+clientMsg);

        Parser.Lexer lexer = new MyLexer(new StringReader(clientMsg));
        Parser parser = new Parser(lexer);

        StringBuilder serverMsg = null;

        try {
            if(parser.parse())
            {
                System.out.print(parser.result);
                serverMsg = parser.result;
                String tem = serverMsg.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        serverMsg.append("$");
        ByteBuf resp = Unpooled.copiedBuffer(serverMsg.toString().getBytes());
        ctx.writeAndFlush(resp);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
