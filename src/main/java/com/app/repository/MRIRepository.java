package com.app.repository;

import com.app.model.entity.MRIImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MRIRepository extends JpaRepository<MRIImage, Long> {
    List<MRIImage> findByUserId(Long userId);
    List<MRIImage> findByUserIdAndBodyPart(Long userId, String bodyPart);
}
