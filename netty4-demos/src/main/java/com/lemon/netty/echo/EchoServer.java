package com.lemon.netty.echo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws InterruptedException {
        new EchoServer(8080).start();
    }

    public void start() throws InterruptedException {
        EchoServerHandler handler = new EchoServerHandler();
        // 1、创建 EventLoopGroup
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 2、创建 ServerBootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(group)
                    // 3、指定使用 NIO 传输 channel
                    .channel(NioServerSocketChannel.class)
                    // 4、设置套接字
                    .localAddress(new InetSocketAddress(port))
                    // 5、添加一个 EchoServerHandler 到子 Channel 的 ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(handler);
                        }
                    });
            // 6、异步绑定服务器，调用sync()方法阻塞等待直到绑定完成
            ChannelFuture future = serverBootstrap.bind().sync();
            // 7、获取Channel的CloseFuture，并且阻塞当前线程直到完成
            future.channel().closeFuture().sync();
        } finally {
            // 8、关闭 EventLoopGroup，适当所有资源
            group.shutdownGracefully().sync();
        }
    }
}
