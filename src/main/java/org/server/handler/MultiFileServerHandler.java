package org.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.parser.MyLexer;
import org.parser.Parser;
import org.util.StringUtil;

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
            System.out.println(Thread.currentThread().getId()+"接收第" + clientNum + "条客户端消息:");

//            Parser.Lexer lexer = new MyLexer(new StringReader(clientMsg));
//            Parser parser = new Parser(lexer);
//
//            StringBuilder serverMsg = null;
//
//
//            try {
//                if (parser.parse()) {
//                    System.out.println(parser.result);
//                    serverMsg = parser.result;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            serverMsg.append("\n");

            StringBuilder serverMsg = addLong(clientMsg);

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

    public StringBuilder addLong(String str)
    {
        int pre = 0;
        long result = 0L;
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < str.length(); i++)
        {
            switch (str.charAt(i))
            {
                case ',':
                {
                    result += Long.parseLong(str.substring(pre, i));
                    pre =  i + 1;
                    break;
                }
                case '\r':
                {
                    result += Long.parseLong(str.substring(pre, i));
                    sb.append(result);
                    sb.append("\r\n");
                    result = 0;
                    pre =  i + 1;
                    break;
                }
                case '\n':
                {
                    pre =  i + 1;
                    break;
                }
                default:
                {

                }
            }
        }

        return sb;
    }
}
