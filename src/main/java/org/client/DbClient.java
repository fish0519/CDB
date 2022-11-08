package org.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.string.StringDecoder;
import org.apache.commons.lang.SystemUtils;
import org.client.handler.FileClientHandler;
import org.util.MyMapFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URLDecoder;

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
                b.channel(EpollSocketChannel.class);
            }else{
                b.channel(NioSocketChannel.class);
            }


            b.option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_RCVBUF, 1024*128)
                    .option(ChannelOption.SO_SNDBUF, 1024*128)
                    .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
                    .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
//                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    ch.pipeline().addLast("gzipDecoder", new JdkZlibDecoder());
                    ch.pipeline().addLast("gzipEncoder", new JdkZlibEncoder(9));
                    ByteBuf delimiter = Unpooled.copiedBuffer("$".getBytes());
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(10240, delimiter));
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
        String readFileName = DbClient.class.getResource("/1.txt").getPath();
        readFileName = URLDecoder.decode(readFileName, "UTF-8");
        String writeFileName = DbClient.class.getResource("/2.txt").getPath();
        writeFileName = URLDecoder.decode(writeFileName, "UTF-8");

        System.out.println(readFileName);


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
            MyMapFile readMapFile = new MyMapFile(readFile, 498, "r");

            File writeFile = new File(writeFileName);
            MyMapFile writeMapFile = new MyMapFile(writeFile, 500, "rw");

            new DbClient().connect(port, host, readMapFile, writeMapFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
