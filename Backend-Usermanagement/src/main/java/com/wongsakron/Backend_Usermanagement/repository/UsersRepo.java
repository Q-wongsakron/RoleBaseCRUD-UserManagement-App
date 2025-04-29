package com.wongsakron.Backend_Usermanagement.repository;

import com.wongsakron.Backend_Usermanagement.entity.OurUsers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepo extends JpaRepository<OurUsers, Integer> {

    Optional<OurUsers> findByEmail(String email);
}
