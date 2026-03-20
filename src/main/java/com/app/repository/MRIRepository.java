package com.app.repository;

import com.app.model.entity.MRIImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MRIRepository extends JpaRepository<MRIImage, Long> {
    List<MRIImage> findByUserId(Long userId);
    List<MRIImage> findByUserIdAndBodyPart(Long userId, String bodyPart);
    void deleteByFileUrl(String fileUrl);
    Optional<MRIImage> findByFileUrl(String fileUrl);
}
