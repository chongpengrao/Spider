package com.pn.server;

import com.pn.http.HttpRequest;
import com.pn.http.HttpResponse;
import com.pn.pojo.HttpConstant;
import com.pn.pojo.ServerConstant;

import java.io.*;
import java.net.Socket;

public class HttpHandler implements Runnable {

    private Socket socket;
    private File file = null;

    public HttpHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            HttpRequest request = new HttpRequest(socket.getInputStream());
            HttpResponse response = new HttpResponse(socket.getOutputStream());

            response.setProtocol(ServerConstant.protocol);
            response.setStatus(HttpConstant.CODE_OK);
            //获取请求资源路径
            String pathName = ServerConstant.webRoot + request.getUri();
            file = new File(pathName);
            //未输入uri返回首页
            if ("/".equals(request.getUri())) {
                file = new File(ServerConstant.webRoot + ServerConstant.homePage);
            } else if (!file.exists()) {
                //请求资源找不到时就返回404页面
                file = new File(ServerConstant.webRoot + ServerConstant.notFoundPage);
                response.setStatus(HttpConstant.CODE_NOTFOUND);
            }
            String fileName = file.getName();
            response.setContentType(ServerConstant.typeMap.get(fileName.substring(fileName.lastIndexOf(".") + 1)));
            response.setContentLength(file.length());

            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            //输出资源给客户端
            String range = request.getRange();
            if (range != null && !range.isEmpty()) {
                response.setStatus(HttpConstant.CODE_CONTINUE);
                String[] indexArray = range.split("=")[1].split("-");
                long startIndex = Integer.parseInt(indexArray[0]);
                long endIndex = Integer.parseInt(indexArray[1]);
                response.setContentRange("bytes=" + startIndex + "-" + endIndex);
                //todo 给出对应范围内的字节
                int total = (int) (endIndex - startIndex);
                inputStream.read(new byte[(int)startIndex]);
                byte[] bytes1 = new byte[total];
                inputStream.read(bytes1);
                response.getOutputStream().write(bytes1);

//                RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
//                accessFile.seek(startIndex);
//                byte[] bytes = new byte[1024];
//                while (total > 0) {
//                    accessFile.readFully(bytes);
//                    if (total < bytes.length) {
//                        response.getOutputStream().write(bytes, 0, total);
//                    } else {
//                        response.getOutputStream().write(bytes);
//                    }
//                    total -= bytes.length;
//                }
            }else {

                byte[] bs = new byte[1024];
                int len = -1;
                while ((len = inputStream.read(bs)) > 0) {
                    response.getOutputStream().write(bs, 0, len);
                }
            }

            response.getOutputStream().flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
