package com.app.repository;

import com.app.model.entity.BloodTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodTestRepository extends JpaRepository<BloodTest, Long> {
    List<BloodTest> findByUserIdOrderByUploadDateDesc(Long userId);
}
