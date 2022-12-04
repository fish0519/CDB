package org.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang.SystemUtils;
import org.client.DbClient;
import org.client.handler.FileClientHandler;
import org.client.handler.MultiFileClientHandler;
import org.server.handler.FileServerHandler;
import org.server.handler.MultiFileServerHandler;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;

public class DbServer {

    private final boolean isEpollEnabled;

    public static Properties properties = new Properties();
    static {
        try {
            String clientFile = DbClient.class.getResource("/server.properties").getPath();
            clientFile = URLDecoder.decode(clientFile, "UTF-8");

            properties.load(new InputStreamReader(new FileInputStream(clientFile)));
            System.setProperty("serverMode", properties.getProperty("serverMode").trim());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            b.option(ChannelOption.TCP_NODELAY, true)
             .option(ChannelOption.SO_RCVBUF, 1024*1024)
             .option(ChannelOption.SO_SNDBUF, 1024*1024)
             .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
             .childHandler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
//                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    ch.pipeline().addLast("gzipDecoder", new JdkZlibDecoder());
                    ch.pipeline().addLast("gzipEncoder", new JdkZlibEncoder(9));
                    ByteBuf delimiter = Unpooled.copiedBuffer("$".getBytes());
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024*1024, delimiter));
                    ch.pipeline().addLast(new StringDecoder());
                    if(System.getProperty("serverMode").equals("1"))
                    {
                        ch.pipeline().addLast(new FileServerHandler());
                    }else{
                        ch.pipeline().addLast(new MultiFileServerHandler());
                    }
                }
            });

            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        DbServer dbServer = new DbServer();
        try {
            int port = Integer.parseInt(properties.getProperty("port").trim());
            dbServer.bind(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
