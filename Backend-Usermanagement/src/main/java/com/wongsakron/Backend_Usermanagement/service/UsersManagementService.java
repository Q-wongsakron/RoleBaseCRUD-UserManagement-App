// กำหนด package
package com.wongsakron.Backend_Usermanagement.service;

// import class และ dependency ที่ใช้ในคลาสนี้
import com.wongsakron.Backend_Usermanagement.dto.ReqRes;                 // DTO สำหรับรับ/ส่งข้อมูล request และ response
import com.wongsakron.Backend_Usermanagement.entity.OurUsers;           // Entity ที่แมปกับตาราง users ในฐานข้อมูล
import com.wongsakron.Backend_Usermanagement.repository.UsersRepo;      // Repository ที่ใช้ติดต่อกับฐานข้อมูล users
import org.springframework.security.authentication.AuthenticationManager; // ใช้สำหรับทำ authentication
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // ใช้สร้าง token สำหรับตรวจสอบ username/password
import org.springframework.security.crypto.password.PasswordEncoder;    // สำหรับเข้ารหัส password ก่อนเก็บลงฐานข้อมูล
import org.springframework.stereotype.Service;                          // บอกว่า class นี้เป็น service

import java.util.HashMap; // ใช้เก็บ key-value ในการสร้าง refresh token
import java.util.List;
import java.util.Optional;

// ประกาศว่า class นี้เป็น Spring Service
@Service
public class UsersManagementService {

    // ประกาศ dependency ทั้ง 4 ตัวที่จำเป็นต้องใช้ใน class นี้
    private final UsersRepo usersRepo;
    private final JWTUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    // Constructor สำหรับ inject dependency ทั้งหมดเข้ามา
    public UsersManagementService(UsersRepo usersRepo, JWTUtils jwtUtils, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    // ฟังก์ชันสำหรับลงทะเบียนผู้ใช้ใหม่
    public ReqRes register(ReqRes registrationRequest){
        ReqRes resp = new ReqRes(); // สร้าง object สำหรับเก็บผลลัพธ์การทำงาน

        try {
            OurUsers ourUser = new OurUsers(); // สร้าง object ผู้ใช้ใหม่
            // กำหนดค่า field จาก request ที่รับเข้ามา
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setCity(registrationRequest.getCity());
            ourUser.setRole(registrationRequest.getRole());
            ourUser.setName(registrationRequest.getName());
            // เข้ารหัส password ก่อนบันทึก
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            // บันทึกผู้ใช้ลงในฐานข้อมูล
            OurUsers ourUsersResult = usersRepo.save(ourUser);
            // ตรวจสอบว่ามีการบันทึกจริง (id ต้องมากกว่า 0)
            if(ourUsersResult.getId() > 0){
                resp.setOurUsers(ourUsersResult);
                resp.setMessage("User Saved Successfully"); // ส่งข้อความสำเร็จ
                resp.setStatusCode(200);                    // HTTP Status Code: OK
            }
        }catch (Exception e) {
            resp.setStatusCode(500);           // ถ้าเกิด error ส่ง status 500
            resp.setError(e.getMessage());     // บันทึกข้อความ error
        }
        return resp; // คืนค่าผลลัพธ์
    }

    // ฟังก์ชันสำหรับ login ผู้ใช้
    public ReqRes login(ReqRes loginRequest){
        ReqRes response = new ReqRes(); // สร้าง object สำหรับเก็บผลลัพธ์การ login

        try {
            // ตรวจสอบ username และ password ด้วย AuthenticationManager
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            // หากผ่านจะโหลดข้อมูล user จาก email
            var user = usersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            // สร้าง JWT token และ refresh token
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            // กำหนดค่าต่าง ๆ ลงใน response
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");

        }catch (Exception e){
            response.setStatusCode(500);             // กรณีเกิด error
            response.setError((e.getMessage()));     // ส่งข้อความ error กลับไป
        }
        return response; // คืนค่าผลลัพธ์
    }

    public ReqRes refreshToken(ReqRes refreshToken){
        ReqRes response = new ReqRes(); // สร้างอ็อบเจ็กต์ response สำหรับเก็บผลลัพธ์

        try{
            // ดึงอีเมลจาก refresh token ที่แนบมา
            String ourEmail = jwtUtils.extractUsername(refreshToken.getToken());

            // ค้นหาผู้ใช้งานจากฐานข้อมูลโดยใช้อีเมล
            OurUsers user = usersRepo.findByEmail(ourEmail).orElseThrow();

            // ตรวจสอบว่า token ที่ส่งมายังถูกต้องและใช้ได้
            if(jwtUtils.isTokenValid(refreshToken.getToken(), user)){

                // สร้าง JWT ตัวใหม่สำหรับผู้ใช้คนนี้
                var jwt = jwtUtils.generateToken(user);

                // ตั้งค่าข้อมูล response เมื่อรีเฟรช token สำเร็จ
                response.setStatusCode(200);
                response.setToken(jwt); // JWT ใหม่
                response.setRefreshToken(refreshToken.getToken()); // ใช้ refresh token เดิม
                response.setExpirationTime("24Hrs"); // ระยะเวลาหมดอายุ
                response.setMessage("Successfully Refreshed Token"); // ข้อความแจ้งผล
            }

            response.setStatusCode(200); // ส่งกลับ status OK
            return response; // คืนผลลัพธ์กลับ

        } catch (Exception e) {
            // หากเกิดข้อผิดพลาดในการรีเฟรช token
            response.setStatusCode(500); // Internal Server Error
            response.setError(e.getMessage()); // ส่งข้อความ error กลับ
            return response;
        }
    }


    public ReqRes getAllUsers() {
        ReqRes response = new ReqRes(); // สร้าง response object

        try{
            // ดึงรายชื่อผู้ใช้ทั้งหมดจากฐานข้อมูล
            List<OurUsers> result = usersRepo.findAll();

            if(!result.isEmpty()){
                // หากพบข้อมูลผู้ใช้
                response.setOurUsersList(result); // ตั้งค่ารายชื่อผู้ใช้
                response.setStatusCode(200); // ส่งสถานะ OK
                response.setMessage("Successful"); // ข้อความแจ้งผลสำเร็จ
            } else {
                // หากไม่พบผู้ใช้เลย
                response.setStatusCode(404); // Not Found
                response.setMessage("No users found"); // แจ้งว่าไม่พบผู้ใช้
            }

            return response; // คืนค่ากลับ

        } catch (Exception e) {
            // จัดการกรณีเกิดข้อผิดพลาด
            response.setStatusCode(500); // Internal Server Error
            response.setError(e.getMessage()); // แสดงข้อความ error
            return response;
        }
    }


    public ReqRes getUsersById(Integer id){
        ReqRes response = new ReqRes(); // สร้าง response object

        try{
            // ค้นหาผู้ใช้จาก id
            OurUsers usersById = usersRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("User Not Found")); // ถ้าไม่เจอจะ throw error

            // ตั้งค่าข้อมูลผู้ใช้ที่พบ
            response.setOurUsers(usersById);
            response.setStatusCode(200); // OK
            response.setMessage("Users with id '" + id + "' found successfully"); // ข้อความแจ้งสำเร็จ

        } catch (Exception e){
            // หากมีข้อผิดพลาด
            response.setStatusCode(500); // Internal Server Error
            response.setError(e.getMessage()); // แจ้ง error
        }

        return response; // คืน response กลับ
    }

    // Service class สำหรับจัดการข้อมูลผู้ใช้ เช่น ลบ แก้ไข และดึงข้อมูล
    public ReqRes deleteUser(Integer userId){
        ReqRes response = new ReqRes(); // สร้าง Response object เพื่อใช้ส่งผลลัพธ์กลับ
        try{
            Optional<OurUsers> userOptional = usersRepo.findById(userId); // ค้นหาผู้ใช้ตาม ID
            if(userOptional.isPresent()){ // ถ้าพบผู้ใช้
                usersRepo.deleteById(userId); // ลบผู้ใช้จากฐานข้อมูล
                response.setStatusCode(200); // ตั้งสถานะว่าเสร็จสมบูรณ์
                response.setMessage("User deleted successfully"); // ข้อความบอกผล
            } else{
                response.setStatusCode(404); // ไม่พบผู้ใช้
                response.setMessage("User not found for deletion"); // ข้อความแจ้งว่าไม่เจอ
            }
        }catch (Exception e){
            response.setStatusCode(500); // กรณีเกิดข้อผิดพลาด เช่น DB ล่ม
            response.setError("Error occurred while deleting user: " + e.getMessage()); // แสดงข้อความ error
        }
        return response; // ส่งผลลัพธ์กลับ
    }

    public ReqRes updateUser(Integer userId, OurUsers updateUser){
        ReqRes response = new ReqRes(); // สร้าง Response object
        try{
            Optional<OurUsers> userOptional = usersRepo.findById(userId); // ค้นหาผู้ใช้จาก ID
            if(userOptional.isPresent()){ // ถ้ามีข้อมูลผู้ใช้อยู่
                OurUsers existingUser = userOptional.get(); // ดึงข้อมูลผู้ใช้เดิม

                // เซตค่าข้อมูลใหม่ที่รับเข้ามา
                existingUser.setEmail(updateUser.getEmail());
                existingUser.setName(updateUser.getName());
                existingUser.setCity(updateUser.getCity());
                existingUser.setRole(updateUser.getRole());

                // ถ้ามีการส่งรหัสผ่านใหม่มาด้วย และไม่ว่าง
                if(updateUser.getPassword() != null && !updateUser.getPassword().isEmpty()){
                    // เข้ารหัสรหัสผ่านก่อนอัปเดต
                    existingUser.setPassword(passwordEncoder.encode(updateUser.getPassword()));
                }

                OurUsers savedUser = usersRepo.save(existingUser); // บันทึกการเปลี่ยนแปลงลงฐานข้อมูล
                response.setOurUsers(savedUser); // แนบข้อมูลผู้ใช้ใหม่ใน response
                response.setStatusCode(200); // ตั้งสถานะสำเร็จ
                response.setMessage("User updated successfully"); // ข้อความแจ้งผลลัพธ์
            } else {
                response.setStatusCode(404); // ถ้าไม่พบผู้ใช้
                response.setMessage("User not found for update"); // ข้อความแจ้งผลลัพธ์
            }
        }catch (Exception e){
            response.setStatusCode(500); // หากมีข้อผิดพลาดเกิดขึ้น
            response.setError("Error occurred while updating user: " + e.getMessage()); // รายละเอียด error
        }
        return response; // ส่งผลลัพธ์กลับ
    }

    public ReqRes getMyInfo(String email){
        ReqRes response = new ReqRes(); // สร้าง response object

        try{
            Optional<OurUsers> userOptional = usersRepo.findByEmail(email); // ค้นหาผู้ใช้จาก email
            if(userOptional.isPresent()){ // ถ้าเจอ
                response.setOurUsers(userOptional.get()); // ใส่ข้อมูลผู้ใช้ใน response
                response.setStatusCode(200); // สถานะสำเร็จ
                response.setMessage("successful"); // ข้อความผลลัพธ์
            }else{
                response.setStatusCode(404); // ไม่เจอผู้ใช้
                response.setMessage("User not found for update"); // ข้อความผลลัพธ์
            }
        }catch (Exception e){
            response.setStatusCode(500); // ถ้ามี error
            response.setError("Error occurred while getting user info: " + e.getMessage()); // รายละเอียด error
        }
        return response; // ส่ง response กลับ
    }


}
