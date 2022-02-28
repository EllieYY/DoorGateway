package com.wimetro.acs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

//    @Test
//    void contextLoads() {
//    }

    @Test
    void decodeIp() {
        String msgBodyStr = "1080;0010;192.168.2.205;";
        int index1 = msgBodyStr.lastIndexOf(";", -2);
        System.out.println(index1);
    }

}
