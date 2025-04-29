package com.wongsakron.Backend_Usermanagement.service; // ระบุ package ที่ไฟล์นี้อยู่

// นำเข้าคลาสและ annotation ที่จำเป็น
import com.wongsakron.Backend_Usermanagement.repository.UsersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service // บอก Spring ว่านี่คือ Service Component (Spring จะสร้าง Bean ให้อัตโนมัติ)
public class OurUserDetailsService implements UserDetailsService { // implements UserDetailsService เพื่อใช้โหลดข้อมูลผู้ใช้สำหรับการล็อกอิน

    private final UsersRepo usersRepo; // ประกาศตัวแปรเพื่อใช้เข้าถึงข้อมูลผู้ใช้จากฐานข้อมูล

    @Autowired // ใช้ Constructor Injection เพื่อให้ Spring ฉีด UsersRepo เข้ามา
    public OurUserDetailsService(UsersRepo usersRepo) {
        this.usersRepo = usersRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // เมธอดที่ Spring Security จะเรียกเมื่อต้องการโหลดข้อมูลผู้ใช้ตาม username (ในที่นี้คือ email)

        // เรียกใช้ UsersRepo เพื่อค้นหาผู้ใช้ตาม email
        // ถ้าไม่พบผู้ใช้ จะโยน Exception ออกมา
        return usersRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
