package com.wongsakron.Backend_Usermanagement.config;

// import คลาสและ annotation ที่เกี่ยวข้องกับความปลอดภัย
import com.wongsakron.Backend_Usermanagement.service.OurUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // ระบุว่าคลาสนี้ใช้สำหรับกำหนดค่า config
@EnableWebSecurity // เปิดใช้ Spring Security
public class SecurityConfig {

    private final OurUserDetailsService ourUserDetailsService; // service สำหรับดึงข้อมูลผู้ใช้จาก database
    private final JWTAuthFilter jwtAuthFilter; // filter สำหรับตรวจสอบ JWT token

    // constructor สำหรับฉีด dependencies เข้ามา
    public SecurityConfig(OurUserDetailsService ourUserDetailsService, JWTAuthFilter jwtAuthFilter) {
        this.ourUserDetailsService = ourUserDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean // สร้าง Bean สำหรับ SecurityFilterChain ที่จะจัดการการเข้าถึง endpoint ต่าง ๆ
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable) // ปิดการใช้ CSRF เพราะเราใช้ JWT
                .cors(Customizer.withDefaults()) // เปิดใช้งาน CORS
                .authorizeHttpRequests(request -> request
                        // อนุญาตให้ path เหล่านี้เข้าถึงได้โดยไม่ต้องล็อกอิน
                        .requestMatchers("/auth/**", "/public/**").permitAll()
                        // เฉพาะ role ADMIN เท่านั้นที่เข้าถึง path นี้ได้
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN")
                        // เฉพาะ USER เท่านั้นที่เข้าถึง path นี้ได้
                        .requestMatchers("/user/**").hasAnyAuthority("USER")
                        // ทั้ง ADMIN และ USER เข้าถึงได้
                        .requestMatchers("/adminuser/**").hasAnyAuthority("ADMIN", "USER")
                        // คำขออื่น ๆ ต้องล็อกอินก่อน
                        .anyRequest().authenticated())
                // กำหนดให้ไม่สร้าง session ใหม่ เพราะเราใช้ JWT แบบ stateless
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // กำหนด provider ที่ใช้ตรวจสอบผู้ใช้ และเพิ่ม jwt filter ไว้ก่อน filter หลัก
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build(); // สร้างและคืนค่าคอนฟิก
    }

    @Bean // Bean สำหรับกำหนดว่าใช้ DaoAuthenticationProvider ในการตรวจสอบผู้ใช้
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(ourUserDetailsService); // เซ็ต service สำหรับดึงผู้ใช้
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder()); // เซ็ต encoder สำหรับเช็ครหัสผ่าน
        return daoAuthenticationProvider;
    }

    @Bean // สร้าง Bean สำหรับเข้ารหัสรหัสผ่านด้วย BCrypt
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean // สร้าง Bean สำหรับ AuthenticationManager ใช้จัดการ authentication โดยรวม
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // คืนค่า Manager ที่ Spring จัดการให้
    }
}
