package com.demo_shopyy_1.repository;

import com.demo_shopyy_1.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    User findUserByEmail(String email);

    @Query("select r.name from User u join u.roles r where u.email = :email")
    List<String> findRolesByEmail(@Param("email") String email);
}
