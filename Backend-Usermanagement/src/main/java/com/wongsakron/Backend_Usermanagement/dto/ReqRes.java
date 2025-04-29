package com.wongsakron.Backend_Usermanagement.dto;

// import สำหรับการทำ JSON Serialize/Deserialize
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wongsakron.Backend_Usermanagement.entity.OurUsers;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL) // ถ้าฟิลด์ไหนเป็น null จะไม่ serialize ออกไปใน JSON
@JsonIgnoreProperties(ignoreUnknown = true) // ถ้า JSON มี field ที่ class นี้ไม่รู้จัก จะไม่ throw error
public class ReqRes {

    private int statusCode;
    private String error;
    private String message;

    private String token;
    private String refreshToken;
    private String expirationTime;

    private String name;
    private String city;
    private String role;
    private String email;
    private String password;

    private OurUsers ourUsers;
    private List<OurUsers> ourUsersList;
}
