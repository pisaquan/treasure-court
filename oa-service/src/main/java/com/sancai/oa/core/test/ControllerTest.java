package com.sancai.oa.core.test;


import com.alibaba.fastjson.JSONObject;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.net.URL;

/**
 * @Author chenm
 * @create 2019/7/23 11:38
 */
public class ControllerTest {
    @LocalServerPort
    private int port;

    private URL base;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        String url = String.format("http://localhost:%d/", port);
        System.out.println(String.format("port is : [%d]", port));
        this.base = new URL(url);
    }


    protected JSONObject get(String method) throws Exception {

        ResponseEntity<JSONObject> response = this.restTemplate.getForEntity(
                this.base.toString() + method, JSONObject.class, "");

        return response.getBody();
    }

    protected JSONObject post(String method,Object dto) throws Exception {

        ResponseEntity<JSONObject> response = restTemplate.postForEntity(this.base.toString() + method, dto, JSONObject.class);
        return response.getBody();
    }

}
