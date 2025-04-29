package com.wongsakron.Backend_Usermanagement.service;

// นำเข้า libraries ที่จำเป็น
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;

@Component // ระบุว่าเป็น Bean ของ Spring (Spring จะสร้าง instance ให้อัตโนมัติ)
public class JWTUtils {

    private SecretKey Key; // ตัวแปรเก็บ Secret Key สำหรับเข้ารหัส/ถอดรหัส JWT
    private static final long EXPIRATION_TIME = 86400000; // เวลาหมดอายุของ Token (24 ชั่วโมง)

    // Constructor สำหรับสร้าง Key ตั้งแต่ตอนสร้างอ็อบเจกต์
    public JWTUtils(){
        // กำหนดค่า Secret เป็น String ยาวๆ
        String secreteString = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";

        // แปลง Secret จาก String เป็น byte array แล้ว decode ด้วย Base64
        byte[] keyBytes = Base64.getDecoder().decode(secreteString.getBytes(StandardCharsets.UTF_8));

        // สร้าง SecretKey สำหรับใช้เข้ารหัส/ถอดรหัส โดยใช้ HMAC SHA256
        this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // เมธอดสำหรับสร้าง JWT Access Token โดยใช้ข้อมูลจาก UserDetails
    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername()) // ใส่ username ลงใน subject
                .issuedAt(new Date(System.currentTimeMillis())) // เวลาที่สร้าง Token
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // เวลาหมดอายุ
                .signWith(Key) // เซ็น Token ด้วย Secret Key
                .compact(); // สร้าง Token ออกมาในรูปแบบ String
    }

    // เมธอดสำหรับสร้าง Refresh Token ที่สามารถเก็บ Claims เพิ่มเติมได้
    public String generateRefreshToken(HashMap<String, Objects> claims, UserDetails userDetails){
        return Jwts.builder()
                .claims(claims) // เพิ่มข้อมูลเพิ่มเติม (claims) ลงใน Token
                .subject(userDetails.getUsername()) // ใส่ username
                .issuedAt(new Date(System.currentTimeMillis())) // เวลาที่สร้าง Token
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // เวลาหมดอายุ
                .signWith(Key) // เซ็นด้วย Secret Key
                .compact(); // สร้างออกมาเป็น String
    }

    // ดึง username ออกมาจาก Token
    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject); // ดึง Subject จาก Claims
    }

    // เมธอด generic สำหรับดึงข้อมูลจาก Claims ของ Token
    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        // parseSignedClaims = ถอดลายเซ็นแล้วอ่าน payload (ข้อมูลใน Token)
        return claimsTFunction.apply(Jwts.parser()
                .verifyWith(Key) // ใช้ Key ในการตรวจสอบความถูกต้องของลายเซ็น
                .build()
                .parseSignedClaims(token)
                .getPayload()); // เอาเฉพาะ payload มาดึงข้อมูล
    }

    // ตรวจสอบว่า Token ใช้ได้หรือไม่ (Username ตรงกัน และ Token ยังไม่หมดอายุ)
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ตรวจสอบว่า Token หมดอายุแล้วหรือยัง
    public boolean isTokenExpired(String token){
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }
}
