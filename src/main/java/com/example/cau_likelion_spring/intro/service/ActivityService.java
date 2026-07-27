package com.example.cau_likelion_spring.intro.service;

import com.example.cau_likelion_spring.intro.domain.Activity;
import com.example.cau_likelion_spring.intro.dto.ActivityRequestDto;
import com.example.cau_likelion_spring.intro.dto.ActivityResponseDto;
import com.example.cau_likelion_spring.intro.repository.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Transactional
    public ActivityResponseDto createActivity(ActivityRequestDto request) {
        Activity activity = Activity.builder()
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .introduction(request.getIntroduction())
                .description(request.getDescription())
                .buttonName(request.getButtonName())
                .pageNavigation(request.getPageNavigation())
                .build();

        return ActivityResponseDto.from(activityRepository.save(activity));
    }

    public List<ActivityResponseDto> getActivityList() {
        return activityRepository.findAll().stream()
                .map(ActivityResponseDto::from)
                .toList();
    }

    @Transactional
    public ActivityResponseDto updateActivity(Long id, ActivityRequestDto request) {
        Activity activity = findActivityById(id);
        activity.update(
                request.getName(), request.getImageUrl(), request.getIntroduction(),
                request.getDescription(), request.getButtonName(), request.getPageNavigation()
        );
        return ActivityResponseDto.from(activity);
    }

    @Transactional
    public void deleteActivity(Long id) {
        activityRepository.delete(findActivityById(id));
    }

    private Activity findActivityById(Long id) {
        return activityRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 활동입니다. id=" + id));
    }
}
