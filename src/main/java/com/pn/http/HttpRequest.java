package com.pn.http;

import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Data
public class HttpRequest {
    private String method;//请求方式
    private String uri;//请求资源路径
    private String protocol;//请求协议
    //get请求参数
    private Map<String,String> paramMap = new HashMap<String, String>();

    public HttpRequest(InputStream in){
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            //请求行：  GET /index.html HTTP/1.1
            String requestLine = reader.readLine();
            if (requestLine==null){
                throw new RuntimeException("请求行有误。");
            }
            String[] requestLineArray = requestLine.split(" ");
            method = requestLineArray[0];
            uri = requestLineArray[1];
            protocol = requestLineArray[2];
            if (uri != null && uri.contains("?")){
                String[] requestParams = uri.split("?")[1].split("&");
                for (String str : requestParams){
                    paramMap.put(str.split("=")[0],str.split("=")[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
