package org.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.parser.MyLexer;
import org.parser.Parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiFileServerHandler extends ChannelInboundHandlerAdapter {

    public static AtomicInteger num = new AtomicInteger(0);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ctx.channel().eventLoop().execute(()->{
            String clientMsg = (String) msg;
            int clientNum = num.incrementAndGet();
            System.out.println();
            System.out.println("第" + clientNum + "条客户端消息:" + clientMsg);

            Parser.Lexer lexer = new MyLexer(new StringReader(clientMsg));
            Parser parser = new Parser(lexer);

            StringBuilder serverMsg = null;

            try {
                if (parser.parse()) {
                    System.out.println(parser.result);
                    serverMsg = parser.result;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            serverMsg.append("\n");
            serverMsg.append("$");
            ByteBuf resp = Unpooled.copiedBuffer(serverMsg.toString().getBytes());
            ctx.writeAndFlush(resp);
        });
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
