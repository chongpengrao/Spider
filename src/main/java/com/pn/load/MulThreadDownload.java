package com.pn.load;

import com.pn.pojo.HttpConstant;
import com.pn.pojo.ServerConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@AllArgsConstructor
@NoArgsConstructor
public class MulThreadDownload {
    private String path;//下载路径
    private String savePath;//下载文件存放路径
    private int threadNum;//线程数量
    private long avgSize;//每个线程下载的字节数
    private static int runningThreadNum = 0;//当前运行的线程数
    private String tempPath=ServerConstant.webRoot+"temp";//临时文件存放位置

    public MulThreadDownload(String path, String savePath, int threadNum) {
        this.path=path;
        this.savePath=savePath;
        this.threadNum=threadNum;
    }


    public static void main(String[] args) {
        try {
//        http://localhost/TaylorSwift.zip
//            https://img.piaoniu.com/poster/30b5235359c03bd0e5dd6319127e77e772e23bc2.jpg
            MulThreadDownload download = new MulThreadDownload("https://img.piaoniu.com/poster/30b5235359c03bd0e5dd6319127e77e772e23bc2.jpg", ServerConstant.webRoot+"download", 3);
            download.download();
            System.out.println("================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**s
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
            avgSize = length/threadNum;
            for (int i=0;i<threadNum;i++){
                long startIndex = i*avgSize;
                long endIndex = (i+1)*avgSize;
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
        private long lastPosition;//上一次下载的位置

        public DownloadThread(int threadId, long startIndex, long endIndex) {
            this.threadId=threadId;
            this.startIndex=startIndex;
            this.endIndex=endIndex;
            this.lastPosition = startIndex;
        }

        @Override
        public void run() {
            synchronized (DownloadThread.class){
                runningThreadNum++;
            }

            //将当前线程已经下载的文件字节总数保存到临时文件中
            try {
                HttpURLConnection connection = getConnection(path);

                System.out.println("线程"+threadId+"的任务：从"+startIndex+"字节下载到"+endIndex+"处");
                File file = new File(tempPath,threadId + ".txt");//临时文件存放
                //判断文件是新的下载任务还是继续之前的下载任务
                if (file.exists()){
                    //继续完成上次未下载完成的任务
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file)));
                    lastPosition = Integer.parseInt(bufferedReader.readLine());
                }
                //设置分段下载的头信息
                connection.setRequestProperty("Range","bytes="+lastPosition+"-"+endIndex);

                System.out.println("线程"+threadId+"实际下载：从"+lastPosition+"到"+endIndex);

                //206 - 资源请求成功
                if (connection.getResponseCode()==HttpConstant.CODE_CONTINUE){
                    InputStream inputStream = connection.getInputStream();
                    //将资源写入已经创建好的文件中
                    String fileName = path.substring(path.lastIndexOf("/")+1);
                    RandomAccessFile randomAccessFile = new RandomAccessFile(new File(savePath,fileName), "rw");
                    randomAccessFile.seek(lastPosition);//文件的写入位置
                    byte[] bytes = new byte[1024];
                    int len = -1;
                    long total = 0;//记录本次线程下载的文件大小
                    while ((len=inputStream.read(bytes))>0){

                        randomAccessFile.write(bytes,0,len);
                        total += len;
                        //将下载的文件长度记录到临时文件中
                        RandomAccessFile tempFile = new RandomAccessFile(file, "rwd");
                        tempFile.write(String.valueOf(lastPosition+total).getBytes());
                        tempFile.close();
                    }
                    inputStream.close();
                    randomAccessFile.close();
                    System.out.println("线程"+threadId+"下载完成");
                    //当所有线程下载结束时，删除存放下载记录的临时文件
                    synchronized (DownloadThread.class){
                        runningThreadNum --;
                        if (runningThreadNum==0){
                            System.out.println("所有线程都下载完成！");
                            for (int i=0;i<threadNum;i++){
                                File fileToDelete = new File(tempPath,i + ".txt");
                                System.out.println("删除文件："+fileToDelete.getAbsolutePath());
                                boolean delete = fileToDelete.delete();
                                if(delete){
                                    System.out.println("删除成功");
                                }
                            }
                        }
                    }
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
