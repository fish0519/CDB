package org.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang.SystemUtils;
import org.server.handler.FileServerHandler;

public class DbServer {

    private final boolean isEpollEnabled;

    public DbServer()
    {
        isEpollEnabled = SystemUtils.IS_OS_LINUX;
    }

    public void bind(int port) throws Exception {
        EventLoopGroup bossGroup;
        EventLoopGroup workerGroup;

        if(isEpollEnabled)
        {
            bossGroup = new EpollEventLoopGroup();
            workerGroup = new EpollEventLoopGroup();
        }else{
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
        }

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            if(isEpollEnabled)
            {
                b.channel(EpollServerSocketChannel.class);
            }else{
                b.channel(NioServerSocketChannel.class);
            }
            b.option(ChannelOption.SO_BACKLOG, 1024)
             .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    //\r\n作为分包符
                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new FileServerHandler());
                }
            });

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
