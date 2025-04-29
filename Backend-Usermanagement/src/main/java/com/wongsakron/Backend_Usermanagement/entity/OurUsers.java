package com.wongsakron.Backend_Usermanagement.entity;


import jakarta.persistence.*; // สำหรับทำ ORM กับฐานข้อมูล
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity // บอกว่าเป็น Entity ใช้กับ JPA (เชื่อมกับ table ใน database)
@Table(name = "ourusers") // กำหนดชื่อ table เป็น "ourusers"
@Data // Lombok สร้าง getter/setter, toString, equals, hashCode ให้แบบอัตโนมัติ
public class OurUsers implements UserDetails { // implements UserDetails เพื่อใช้ร่วมกับ Spring Security
    // implements UserDetails → เพื่อให้ Spring Security ใช้สำหรับตรวจสอบสิทธิ์
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ให้ database สร้าง id แบบ auto-increment
    private Integer id;
    private String email;
    private String name;
    private String password;
    private String city;
    private String role; // บทบาทของผู้ใช้ เช่น ROLE_USER, ROLE_ADMIN

    // ========== เมธอดที่ override มาจาก UserDetails ==========

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // คืนค่า role ของ user เป็น List ที่มี SimpleGrantedAuthority
        // getAuthorities() → ดึง role ของ user ให้ Spring เอาไปจัดการเรื่องสิทธิ์
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        // คืนค่า email เป็น username สำหรับ login
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // บอกว่าบัญชีนี้ยังไม่หมดอายุ (true ตลอด)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // บอกว่าบัญชีนี้ไม่ได้ถูกล็อก (true ตลอด)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // บอกว่า credentials (เช่น password) ยังไม่หมดอายุ (true ตลอด)
        return true;
    }

    @Override
    public boolean isEnabled() {
        // บอกว่าบัญชีนี้เปิดใช้งานอยู่ (true ตลอด)
        return true;
    }
}
