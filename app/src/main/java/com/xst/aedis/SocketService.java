package com.xst.aedis;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

/**
 * @author joker
 */
public class SocketService extends Thread {

    /* 标识数字 */
    private int flag = 1;
    /* 缓冲区大小 */
    private int blockSize = 4096;
    /* 接受数据缓冲区 */
    private ByteBuffer sendBuffer = ByteBuffer.allocate(blockSize);
    /* 发送数据缓冲区 */
    private ByteBuffer receiveBuffer = ByteBuffer.allocate(blockSize);
    //打开服务器套接字通道
    ServerSocketChannel serverSocketChannel;
    //检索与此通道关联的服务器套接字
    ServerSocket serverSocket;

    private Selector selector;

    private int port;


    public SocketService(int port) {
        this.port = port;
    }


    public void close() {
        try {
            serverSocketChannel.close();
            selector.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void open() {
        try {

            serverSocketChannel = ServerSocketChannel.open();
            //服务器配置为非阻塞
            serverSocketChannel.configureBlocking(false);

            serverSocket = serverSocketChannel.socket();
            //进行服务的绑定
            serverSocket.bind(new InetSocketAddress(port));
            //通过open()方法接到Selector
            selector = Selector.open();
            //注册selector,等待连接
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Server start--->" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        open();
        try {

            int select;
            while ((select = selector.select()) > 0) {
                System.out.println("select size:" + select);
                //返回此选择器的已选择键集
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();
                    //具体业务逻辑
                    try {
                        handleKey(selectionKey);
                    } catch (Exception e) {
                        SelectableChannel channel = selectionKey.channel();
                        channel.close();
                        System.out.println("客户端关闭->" + e.getMessage());
                    }
                }
            }
            System.out.println("服务已关闭。");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //处理请求
    public void handleKey(SelectionKey selectionKey) throws IOException {
        //接收请求
        ServerSocketChannel server = null;
        SocketChannel client = null;
        String receiveText;
        String sendText;
        int count = 0;
        // 测试此键的通道是否已经准备好接收新的套接字连接
        if (selectionKey.isAcceptable()) {
            //返回为之创建此键的通道
            server = (ServerSocketChannel) selectionKey.channel();
            //接受到此通道套接字的连接
            //此方法返回的套接字通道(如果有)将处于阻塞模式。
            client = server.accept();
            System.out.println(client.socket().getInetAddress() + ":" + client.socket().getPort() + " -> selectionKey.isAcceptable()");
            //配置为阻塞
            client.configureBlocking(false);
            //注册selector，等待连接
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            client = (SocketChannel) selectionKey.channel();
            System.out.println(client.socket().getInetAddress() + ":" + client.socket().getPort() + " -> selectionKey.isReadable()");
            // 将缓冲区清空以备下次读取
            receiveBuffer.clear();
            // 读取哭护短发送来的数据到缓冲区中
            count = client.read(receiveBuffer);
            if (count > 0) {
                receiveText = new String(receiveBuffer.array(), 0, count);
                System.out.println("服务端接收到的客户信息：" + receiveText);
                byte[] process = process(receiveBuffer);
                sendBuffer.clear();
                sendBuffer.put(process);
                sendBuffer.flip();
                client.write(sendBuffer);
                client.register(selector, SelectionKey.OP_READ);
            } else if (count == 0) {
                System.out.println("===================================");
                System.out.println(client.isConnectionPending());
                System.out.println(client.finishConnect());
                System.out.println(client.isOpen());
                System.out.println(client.isConnected());
                System.out.println(selectionKey.isValid());
                System.out.println("===================================");
            } else if (count == -1) {
                client.close();
                System.out.println(client.socket().getInetAddress() + ":" + client.socket().getPort() + " -> 客户端关闭");
            }
        } else if (selectionKey.isWritable()) {
            // 将缓冲区清空以备下次写入
            sendBuffer.clear();
            client = (SocketChannel) selectionKey.channel();
            System.out.println(client.socket().getInetAddress() + ":" + client.socket().getPort() + " -> selectionKey.isWritable()");
            //发送数据
            sendText = "msg send to client：" + flag++;
            // 向缓冲区中输入数据
            sendBuffer.put(sendText.getBytes());
            // 将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向哭护短,就要复位
            sendBuffer.flip();
            // 输出到通道
            client.write(sendBuffer);
            System.out.println("服务端发送数据给客户端：" + sendText);
            client.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isConnectable()) {
            SelectableChannel channel = selectionKey.channel();
            System.out.println("channel is open : " + channel.isOpen());
            channel.close();
        }

    }

    public static byte[] process(ByteBuffer bytes) {
        String[] par = null;
        try {
            int count = bytes.position();
            bytes.position(0);
            byte b = bytes.get();
            switch (b) {
                case '*':
                    Integer i = Integer.valueOf(readLine(bytes));
                    String method = "";
                    par = new String[i];
                    for (int j = 0; j < i; j++) {
                        if (bytes.get() == '$') {
                            int cmdLen = Integer.valueOf(readLine(bytes));
                            String cmd = readLine(bytes);
                            par[j] = cmd;
                        } else {
                            return "error cmd\r\n".getBytes();
                        }
                    }
                    method = String.valueOf(par[0]);
                    System.out.println(Arrays.toString(par));
                    RedisService redisService = new RedisService();
                    Method target = redisService.getClass().getMethod(method.toLowerCase(), new Class[]{par.getClass()});
                    //System.out.println(target);
                    byte[] invoke = (byte[]) target.invoke(redisService, new Object[]{par});
                    return invoke;
                default:
                    String s = new String(bytes.array(), 0, count);
                    return (("error cmd:" + s + "\r\n").getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (("error cmd:" + Arrays.toString(par) + "\r\n").getBytes());
    }

    public static String readLine(ByteBuffer bytes) {
        final StringBuilder sb = new StringBuilder();
        while (true) {

            byte b = bytes.get();
            if (b == '\r') {
                byte c = bytes.get();
                if (c == '\n') {
                    break;
                }
                sb.append((char) b);
                sb.append((char) c);
            } else {
                sb.append((char) b);
            }
        }

        final String reply = sb.toString();

        return reply;
    }
}



