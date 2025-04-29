package com.wongsakron.Backend_Usermanagement.config;

// import class ที่จำเป็นสำหรับ JWT และ Spring Security
import com.wongsakron.Backend_Usermanagement.service.JWTUtils;
import com.wongsakron.Backend_Usermanagement.service.OurUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // บอก Spring ให้รู้ว่าสร้าง Bean ของ class นี้
public class JWTAuthFilter extends OncePerRequestFilter { // สร้าง Filter ที่ทำงานแค่ 1 ครั้งต่อ 1 Request

    private final JWTUtils jwtUtils; // ตัวช่วยสำหรับจัดการ JWT
    private final OurUserDetailsService ourUserDetailsService; // โหลดข้อมูลผู้ใช้จาก database

    @Autowired // ฉีด dependencies เข้ามาทาง constructor
    public JWTAuthFilter(JWTUtils jwtUtils, OurUserDetailsService ourUserDetailsService) {
        this.jwtUtils = jwtUtils;
        this.ourUserDetailsService = ourUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // ดึงค่า Authorization Header ออกมา
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        // ถ้าไม่มี header หรือ header ว่าง ก็ข้าม filter นี้ไป
        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // ตัด "Bearer " ด้านหน้าทิ้ง แล้วเก็บ token
        jwtToken = authHeader.substring(7);

        // ดึง email ของผู้ใช้จาก JWT Token
        userEmail = jwtUtils.extractUsername(jwtToken);

        // ถ้าได้ email และยังไม่มีข้อมูลการ login ใน SecurityContext
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // โหลดข้อมูลผู้ใช้จาก email
            UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);

            // ตรวจสอบว่า token ยังใช้ได้อยู่
            if (jwtUtils.isTokenValid(jwtToken, userDetails)) {
                // สร้าง SecurityContext ใหม่
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

                // สร้าง Authentication Token จากข้อมูลผู้ใช้
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                // แนบรายละเอียดเพิ่มเติมของ request เช่น IP address
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // ตั้งค่า authentication ไว้ใน context
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
            }
        }

        // ส่ง request ไปยัง filter ถัดไป หรือ Controller
        filterChain.doFilter(request, response);
    }
}