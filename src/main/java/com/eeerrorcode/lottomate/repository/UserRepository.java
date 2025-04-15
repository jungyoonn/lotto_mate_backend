package com.eeerrorcode.lottomate.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eeerrorcode.lottomate.domain.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
}
