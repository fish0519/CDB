package org.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang.SystemUtils;
import org.client.handler.FileClientHandler;
import org.util.MyMapFile;

import java.io.File;
import java.io.FileNotFoundException;

public class DbClient {
    private final boolean isEpollEnabled;


    public DbClient(){
        this.isEpollEnabled = SystemUtils.IS_OS_LINUX;
    }

    public void connect(int port, String host, final MyMapFile readFile, final MyMapFile writeFile) throws Exception {
        EventLoopGroup group;

        if(isEpollEnabled)
        {
            group = new EpollEventLoopGroup();
        }else{
            group = new NioEventLoopGroup();
        }

        try {
            Bootstrap b = new Bootstrap();
            b.group(group);

            if(isEpollEnabled)
            {
                b.channel(EpollServerSocketChannel.class);
            }else{
                b.channel(NioServerSocketChannel.class);
            }

            b.channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    //\r\n作为分包符
                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    ch.pipeline().addLast(new StringDecoder());
                    ch.pipeline().addLast(new FileClientHandler(readFile, writeFile));
                }
            });

            ChannelFuture f = b.connect(host, port).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8888;
        String host = "127.0.0.1";
        String readFileName = "d:/1.txt";
        String writeFileName = "d:/2.txt";

        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
                host = args[1];
                readFileName = args[2];
                writeFileName = args[2];
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }


        try {
            File readFile = new File(readFileName);
            MyMapFile readMapFile = new MyMapFile(readFile, 1024, "r");

            File writeFile = new File(writeFileName);
            MyMapFile writeMapFile = new MyMapFile(readFile, 1024, "rw");

            new DbClient().connect(port, host, readMapFile, writeMapFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
