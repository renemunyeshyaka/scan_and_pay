package com.scan_and_pay.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scan_and_pay.models.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    List<User> findByRole(String role);
    
    List<User> findByEnabledTrue();
    
    List<User> findByEmailVerifiedTrue();
    
    List<User> findByPhoneVerifiedTrue();
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    @Query("SELECT u FROM User u WHERE u.role = 'USER'")
    List<User> findAllRegularUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= CURRENT_DATE")
    Long countUsersRegisteredToday();
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name%")
    List<User> findByNameContaining(@Param("name") String name);
}