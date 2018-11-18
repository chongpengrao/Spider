package com.pn.load;

import com.pn.pojo.HttpConstant;
import com.pn.pojo.ServerConstant;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

@AllArgsConstructor
public class MulThreadDownload {
    private String path;//下载路径
    private String savePath;//下载文件存放路径
    private int threadNum;//线程数量


    public static void main(String[] args) {
        try {
            MulThreadDownload download = new MulThreadDownload("http://localhost/TaylorSwift.zip", ServerConstant.webRoot+"/download", 3);
            download.download();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 下载文件
     */
    public void download() throws Exception {
        HttpURLConnection connection = getConnection(path);
        if (connection==null){
            throw new RuntimeException("连接失败");
        }
        int code = connection.getResponseCode();
        if (code==HttpConstant.CODE_OK){
            //获取资源大小
            int length = connection.getContentLength();
            //在本地创建一个同样大小的文件用来储存下载的资源
            String fileName = path.substring(path.lastIndexOf("/")+1);
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File(savePath, fileName), "rw");
            randomAccessFile.setLength(length);
            //计算每个线程需要下载的字节数量
            int avgSize = length/threadNum;
            for (int i=0;i<threadNum;i++){
                int startIndex = i*avgSize;
                int endIndex = (i+1)*avgSize;
                //如果是最后一个线程则下载剩下的所有资源（avgSize可能有没除尽的）
                if (i==threadNum-1){
                    endIndex = length-1;
                }
                new Thread(new DownloadThread(i,startIndex,endIndex)).start();
            }
            randomAccessFile.close();

        }
    }

    //download线程
    @Data
    @AllArgsConstructor
    private class DownloadThread implements Runnable{

        private long threadId;
        private  long startIndex;
        private long endIndex;

        @Override
        public void run() {
            try {
                HttpURLConnection connection = getConnection(path);
                //设置分段下载的头信息
                connection.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);

                if (connection.getResponseCode()==HttpConstant.CODE_CONTINUE){
                    InputStream inputStream = connection.getInputStream();
                    String fileName = path.substring(path.lastIndexOf("/")+1);
                    RandomAccessFile randomAccessFile = new RandomAccessFile(new File(savePath, fileName), "rw");
                    randomAccessFile.seek(startIndex);//文件的写入位置
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    while ((len=inputStream.read(bytes))>0){
                        randomAccessFile.write(bytes,0,len);
                    }
                    inputStream.close();
                    randomAccessFile.close();
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private HttpURLConnection getConnection(String path) throws Exception {
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(100000);
        return connection;
    }

}
