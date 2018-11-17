package com.pn.server;

import com.pn.pojo.ServerConstant;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private ServerSocket server;

    private ExecutorService threadPool;

    public HttpServer(){
        try {
            server = new ServerSocket(ServerConstant.port);
            threadPool = Executors.newFixedThreadPool(ServerConstant.maxThread);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(){
        try {
            while (true){
                //接收客户端请求
                Socket socket = server.accept();
                //创建线程
                HttpHandler httpHandler = new HttpHandler(socket);
                threadPool.execute(httpHandler);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        server.start();
    }
}
