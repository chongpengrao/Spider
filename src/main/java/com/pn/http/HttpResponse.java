package com.pn.http;

import com.pn.pojo.HttpConstant;
import lombok.Data;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

@Data
public class HttpResponse {
    private String protocol;//协议名
    private int status;//状态码
    private String contentType;//响应数据的格式
    private long contentLength;//响应数据的长度
    private String contentRange;//响应数据的字节范围
    //存放状态码和描述信息
    private Map<Integer,String> httpMap = new HashMap<Integer, String>();
    private OutputStream outputStream;

    public HttpResponse(OutputStream outputStream){
        this.outputStream = outputStream;
        httpMap.put(HttpConstant.CODE_OK, HttpConstant.DESC_OK);
        httpMap.put(HttpConstant.CODE_NOTFOUND, HttpConstant.DESC_NOTFOUND);
        httpMap.put(HttpConstant.CODE_ERROR, HttpConstant.DESC_ERROR);
        httpMap.put(HttpConstant.CODE_CONTINUE, HttpConstant.DESC_OK);
    }

    //保证响应头只被发送一次
    private boolean isSend;

    public OutputStream getOutputStream() {
        if (!isSend) {
            PrintStream ps = new PrintStream(outputStream);
            // 状态行
            ps.println(protocol + " " + status + " "
                    + httpMap.get(status));
            ps.println("Content-Type:" + contentType);
            ps.println("Content-Length:" + contentLength);
            ps.println("Content-Range:"+contentRange);

            ps.println();

            isSend=true;//改变发送状态
        }
        return outputStream;
    }
}
