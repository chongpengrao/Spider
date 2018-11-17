package com.pn.http;

import com.pn.pojo.HttpConstant;
import lombok.Data;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Data
public class HttpResponse {
    private String protocol;//协议名
    private int status;//状态码
    private String contentType;//响应数据的格式
    private long contentLength;//响应数据的长度
    private String acceptRanges;//响应数据的字节范围
    //存放状态码和描述信息
    private Map<Integer,String> httpMap = new HashMap<Integer, String>();
    private OutputStream outputStream;

    public HttpResponse(OutputStream outputStream){
        this.outputStream = outputStream;
        httpMap.put(HttpConstant.CODE_OK, HttpConstant.DESC_OK);
        httpMap.put(HttpConstant.CODE_NOTFOUND, HttpConstant.DESC_NOTFOUND);
        httpMap.put(HttpConstant.CODE_ERROR, HttpConstant.DESC_ERROR);
    }
}
