package com.app.service;

import com.app.model.dto.HealthProfileRequest;
import com.app.model.entity.User;
import com.app.model.entity.UserDetails;
import com.app.repository.UserDetailsRepository;
import com.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HealthProfileService {

    private final UserDetailsRepository userDetailsRepository;

    public Map<String, Object> processProfile(HealthProfileRequest request, User currentUser) {
        UserDetails userDetails = userDetailsRepository.findById(currentUser.getId())
                .orElse(new UserDetails());

        userDetails.setUser(currentUser);
        userDetails.setWeight(request.getWeight());
        userDetails.setHeight(request.getHeight());
        userDetails.setWaist(request.getWaist());
        userDetails.setAge(request.getAge());
        userDetails.setGender(request.getGender());

        UserDetails saved = userDetailsRepository.save(userDetails);
        return buildHealthResponse(saved);
    }

    public Map<String, Object> getProfileByUserId(User currentUser) {
        return userDetailsRepository.findById(currentUser.getId())
                .map(this::buildHealthResponse)
                .orElse(null);
    }

    private Map<String, Object> buildHealthResponse(UserDetails d) {
        double heightMeters = d.getHeight() / 100;
        double bmi = d.getWeight() / (heightMeters * heightMeters);

        double bodyFat = d.getGender().equalsIgnoreCase("MALE")
                ? 64 - (20 * (d.getHeight() / d.getWaist()))
                : 76 - (20 * (d.getHeight() / d.getWaist()));

        Map<String, Object> response = new HashMap<>();
        response.put("weight", d.getWeight());
        response.put("height", d.getHeight());
        response.put("waist", d.getWaist());
        response.put("age", d.getAge());
        response.put("gender", d.getGender());
        response.put("bmi", Math.round(bmi * 10.0) / 10.0);
        response.put("bodyFatPercentage", Math.round(bodyFat * 10.0) / 10.0);
        response.put("idealWeight", Math.round(22 * (heightMeters * heightMeters)));
        response.put("dailyWaterNeed", Math.round((d.getWeight() * 0.035) * 10.0) / 10.0);

        return response;
    }
}