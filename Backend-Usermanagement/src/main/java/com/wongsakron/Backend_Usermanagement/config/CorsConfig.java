// กำหนด package ของคลาสให้อยู่ใน config
package com.wongsakron.Backend_Usermanagement.config;

// import class ที่เกี่ยวข้องกับการ config และจัดการ CORS
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// บอก Spring ว่าคลาสนี้เป็น configuration class
@Configuration
public class CorsConfig {

    // ประกาศ Bean สำหรับ WebMvcConfigurer เพื่อกำหนดค่า CORS
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        // คืนค่าด้วย anonymous class ที่ implement WebMvcConfigurer
        return new WebMvcConfigurer() {
            // override เมธอด addCorsMappings เพื่อกำหนด policy ของ CORS
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // กำหนดให้ทุก endpoint (/**) สามารถรองรับการเรียกข้ามโดเมนได้
                registry.addMapping("/**")
                        // อนุญาตเฉพาะเมธอด HTTP ที่ระบุไว้เท่านั้น เช่น GET, POST, PUT, DELETE
                        .allowedMethods("GET", "POST" ,"PUT", "DELETE")
                        // อนุญาตให้ทุก origin (ทุกโดเมน) เข้ามาเรียกใช้งานได้
                        .allowedOrigins("*");
            }
        };
    }
}
