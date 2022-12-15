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
import org.client.handler.MultiFileClientHandler;
import org.client.handler.SeqFileClientHandler;
import org.util.MyMapFile;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DbClient {
    private final boolean isEpollEnabled;
    Bootstrap b;
    EventLoopGroup group;

    public static Properties properties = new Properties();
    static {
        try {
            String homePath = new File(System.getProperty("user.dir")).getAbsolutePath();
            properties.load(new InputStreamReader(new FileInputStream(new File(homePath, "conf/client.properties"))));

            int cpuNum = Runtime.getRuntime().availableProcessors();
            if(properties.getProperty("clientNum").trim().length() != 0)
            {
                cpuNum = Integer.parseInt(properties.getProperty("clientNum").trim());
            }
            System.setProperty("clientNum", String.valueOf(cpuNum));
            System.setProperty("clientMode", properties.getProperty("clientMode").trim());
            System.setProperty("mmpSize", properties.getProperty("mmpSize").trim());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DbClient(){
        this.isEpollEnabled = SystemUtils.IS_OS_LINUX;
    }

    public void connect(final MyMapFile readFile, final MyMapFile writeFile) throws Exception {

        if(isEpollEnabled)
        {
            group = new EpollEventLoopGroup();
        }else{
            group = new NioEventLoopGroup();
        }

        try {
            b = new Bootstrap();
            b.group(group);

            if(isEpollEnabled)
            {
                b.channel(EpollSocketChannel.class);
            }else{
                b.channel(NioSocketChannel.class);
            }


            b.option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_RCVBUF, 1024*1024)
                    .option(ChannelOption.SO_SNDBUF, 1024*1024)
                    .option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator(true))
                    .handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
//                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
//                    ch.pipeline().addLast("gzipDecoder", new JdkZlibDecoder());
//                    ch.pipeline().addLast("gzipEncoder", new JdkZlibEncoder(9));
                    ByteBuf delimiter = Unpooled.copiedBuffer("$".getBytes());
                    ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024*1024*2, delimiter));
                    ch.pipeline().addLast(new StringDecoder());
                    if(System.getProperty("clientMode").equals("1"))
                    {
                        ch.pipeline().addLast(new FileClientHandler(readFile, writeFile));
                    }else{
//                        ch.pipeline().addLast(new MultiFileClientHandler(readFile, writeFile));
                        ch.pipeline().addLast(new SeqFileClientHandler(readFile, writeFile));
                    }
                }
            });

        } finally {

        }
    }

    public void realConnect(int port, String host)
    {
        try {
            ChannelFuture f = b.connect(host, port).sync();
            if(System.getProperty("clientMode").equals("1"))
            {
                //阻塞
                f.channel().closeFuture().sync();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if(System.getProperty("clientMode").equals("1"))
            {
                group.shutdownGracefully();
            }
        }
    }

    public static void main(String[] args) throws Exception {

        int port = Integer.parseInt(properties.getProperty("port").trim());
        String host = properties.getProperty("ip").trim();
        String readFileName = properties.getProperty("srcFile").trim();
        if(readFileName.length() == 0)
        {
            readFileName = DbClient.class.getResource("/1.txt").getPath();
        }
        readFileName = URLDecoder.decode(readFileName, "UTF-8");

        String writeFileName = properties.getProperty("dstFile").trim();
        if(writeFileName.length() == 0)
        {
            writeFileName = DbClient.class.getResource("/2.txt").getPath();
        }
        writeFileName = URLDecoder.decode(writeFileName, "UTF-8");

        try {

            int mmpSize = Integer.parseInt(properties.getProperty("mmpSize").trim());

            File readFile = new File(readFileName);
            MyMapFile readMapFile = new MyMapFile(readFile, mmpSize, "r");

            File writeFile = new File(writeFileName);
            MyMapFile writeMapFile = new MyMapFile(writeFile, mmpSize*2, "rw");

            DbClient dbClient = new DbClient();
            dbClient.connect(readMapFile, writeMapFile);

            if(System.getProperty("clientMode").equals("1"))
            {
                dbClient.realConnect(port, host);

            }else{
                int clientNum = Integer.parseInt(System.getProperty("clientNum"));
                for(int i = 0;  i < clientNum; i++)
                {
                    dbClient.realConnect(port, host);
                }
            }
            System.out.println("client start success");

            Thread thread = new Thread(new Runnable() {
                boolean writeFinish = false;
                int writeIndex = 0;
                @Override
                public void run() {
                    while(true)
                    {
                        if(writeMapFile.resultNum.get() == 0 && readMapFile.finishRead.get())
                        {
                            if(writeFinish)
                            {
                                System.out.println("End");
                                break;
                            }
                            writeFinish = true;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        for(int i = 0; i < writeMapFile.resultNum.get(); i++)
                        {
                            byte[] writeByte = (byte[]) writeMapFile.result[writeIndex];
                            if(writeByte != null)
                            {
                                writeMapFile.writeFile(writeByte);
                                writeMapFile.result[writeIndex] = null;
                                writeIndex ++;
                                writeMapFile.resultNum.decrementAndGet();
                            }else{
                                break;
                            }
                        }
                    }
                }
            });
            thread.setDaemon(true);
            thread.start();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
