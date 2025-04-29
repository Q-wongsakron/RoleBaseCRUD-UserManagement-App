// ระบุ package ที่คลาสนี้อยู่
package com.wongsakron.Backend_Usermanagement.controller;

// import class ที่จำเป็นสำหรับการทำงาน
import com.wongsakron.Backend_Usermanagement.dto.ReqRes;
import com.wongsakron.Backend_Usermanagement.entity.OurUsers;
import com.wongsakron.Backend_Usermanagement.service.UsersManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// ระบุว่า class นี้คือ REST Controller สำหรับรับคำขอ HTTP
// ResponseEntity เพื่อควบคุมการตอบกลับ (status code + body)
@RestController
public class UserManagementController {

    // Inject service ที่ใช้จัดการ logic ของผู้ใช้
    private final UsersManagementService usersManagementService;

    // Constructor สำหรับรับ UsersManagementService ที่ Spring จะจัดการให้อัตโนมัติ
    public UserManagementController(UsersManagementService usersManagementService) {
        this.usersManagementService = usersManagementService;
    }

    // Endpoint สำหรับสมัครสมาชิก
    @PostMapping("/auth/register")
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes req){
        // เรียก method จาก service และส่ง response กลับแบบ HTTP 200 OK
        return ResponseEntity.ok(usersManagementService.register(req));
    }

    // Endpoint สำหรับเข้าสู่ระบบ
    @PostMapping("/auth/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.login(req));
    }

    // Endpoint สำหรับ refresh JWT token
    @PostMapping("/auth/refresh")
    public ResponseEntity<ReqRes> refreshToken(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.refreshToken(req));
    }

    // Endpoint สำหรับผู้ดูแลระบบเพื่อดึงข้อมูลผู้ใช้ทั้งหมด
    @GetMapping("/admin/get-all-users")
    public ResponseEntity<ReqRes> getAllUsers(){
        return ResponseEntity.ok(usersManagementService.getAllUsers());
    }

    // Endpoint สำหรับดึงข้อมูลผู้ใช้ตาม ID (เฉพาะ admin)
    @GetMapping("/admin/get-user/{userId}")
    public ResponseEntity<ReqRes> getUserById(@PathVariable Integer userId){
        return ResponseEntity.ok(usersManagementService.getUsersById(userId));
    }

    // Endpoint สำหรับอัปเดตข้อมูลผู้ใช้ตาม ID (เฉพาะ admin)
    @PutMapping("/admin/update/{userId}")
    public ResponseEntity<ReqRes> updateUser(@PathVariable Integer userId, @RequestBody OurUsers req){
        return ResponseEntity.ok(usersManagementService.updateUser(userId, req));
    }

    // Endpoint สำหรับดึงข้อมูลของตัวเอง (เฉพาะคนที่ login แล้ว)
    @GetMapping("/adminuser/get-profile")
    public ResponseEntity<ReqRes> getMyProfile(){
        // ดึง Authentication ปัจจุบันจาก Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName(); // ดึง email ของผู้ใช้ที่ login อยู่
        ReqRes response = usersManagementService.getMyInfo(email); // เรียก service ดึงข้อมูล
        return ResponseEntity.status(response.getStatusCode()).body(response); // คืนค่าพร้อม status code
    }

    // Endpoint สำหรับลบผู้ใช้ตาม ID (เฉพาะ admin)
    @DeleteMapping("/admin/delete/{userId}")
    public ResponseEntity<ReqRes> deleteUser(@PathVariable Integer userId){
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }
}
