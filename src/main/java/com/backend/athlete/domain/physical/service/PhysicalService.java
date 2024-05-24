package com.backend.athlete.domain.physical.service;

import com.backend.athlete.domain.physical.dto.request.SavePhysicalRequest;
import com.backend.athlete.domain.physical.dto.response.GetAllPhysicalResponse;
import com.backend.athlete.domain.physical.dto.response.GetPhysicalResponse;
import com.backend.athlete.domain.physical.dto.response.SavePhysicalResponse;
import com.backend.athlete.domain.physical.model.Physical;
import com.backend.athlete.domain.physical.repository.PhysicalRepository;
import com.backend.athlete.domain.user.model.User;
import com.backend.athlete.domain.user.repository.UserRepository;
import com.backend.athlete.global.exception.ServiceException;
import com.backend.athlete.global.jwt.service.CustomUserDetailsImpl;
import com.backend.athlete.global.util.MathUtils;
import com.backend.athlete.global.util.PhysicalUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class PhysicalService {

    private final PhysicalRepository physicalRepository;
    private final UserRepository userRepository;

    public PhysicalService(PhysicalRepository physicalRepository, UserRepository userRepository) {
        this.physicalRepository = physicalRepository;
        this.userRepository = userRepository;
    }


    public SavePhysicalResponse savePhysical(CustomUserDetailsImpl userPrincipal, SavePhysicalRequest dto) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());

        LocalDate today = LocalDate.now();
        dto.setMeasureDate(today);

        boolean existsSave = physicalRepository.existsByUserAndMeasureDate(findUser, today);
        if (existsSave) {
            throw new IllegalArgumentException("하루에 한번만 입력 하실 수 있습니다.");
        }

        double bmi = MathUtils.roundToTwoDecimalPlaces(PhysicalUtils.calculateBMI(findUser.getWeight(), dto.getHeight()));
        dto.setBmi(bmi);

        double bodyFatPercentage = MathUtils.roundToTwoDecimalPlaces(PhysicalUtils.calculateBodyFatPercentage(dto.getBodyFatMass(), dto.getWeight()));
        dto.setBodyFatPercentage(bodyFatPercentage);

        double visceralFatPercentage = MathUtils.roundToTwoDecimalPlaces(PhysicalUtils.calculateVisceralFatPercentage(bodyFatPercentage));
        dto.setVisceralFatPercentage(visceralFatPercentage);

        double bmr = MathUtils.roundToTwoDecimalPlaces(PhysicalUtils.calculateBMR(dto.getWeight(), dto.getHeight(), 30, findUser.getGender().toString()));
        dto.setBmr(bmr);

        Physical savePhysical = physicalRepository.save(SavePhysicalRequest.toEntity(dto, findUser));

        if (!Objects.equals(findUser.getHeight(), dto.getHeight()) || !Objects.equals(findUser.getWeight(), dto.getWeight())) {
            findUser.updatePhysicalAttributes(dto.getWeight(), dto.getHeight());
            userRepository.save(findUser);
        }

        return SavePhysicalResponse.fromEntity(savePhysical);
    }

    @Transactional
    public GetPhysicalResponse getPhysical(CustomUserDetailsImpl userPrincipal, LocalDate dailyDate) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());

        List<Physical> physicals = physicalRepository.findPhysicalsByUserIdAAndAndMeasureDate(findUser.getId(), dailyDate);

        if (physicals.isEmpty()) {
            throw new ServiceException(dailyDate + " 의 인바디 정보가 존재하지 않습니다.");
        }

        Physical physical = physicals.get(0);

        return GetPhysicalResponse.fromEntity(physical);
    }

    @Transactional
    public Page<GetAllPhysicalResponse> getPhysicalData(CustomUserDetailsImpl userPrincipal, int page, int size) {
        User user = userRepository.findByUserId(userPrincipal.getUsername());
        if (user == null) {
            throw new ServiceException("User not found.");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Physical> physicalPage = physicalRepository.findByUserIdOrderByMeasureDateDesc(user.getId(), pageable);

        List<GetAllPhysicalResponse> responses = new ArrayList<>();
        Physical previousPhysical = null;

        for (Physical physical : physicalPage) {
            GetAllPhysicalResponse response = GetAllPhysicalResponse.fromEntity(physical, previousPhysical);
            previousPhysical = physical;
            responses.add(response);
        }

        return new PageImpl<>(responses, pageable, physicalPage.getTotalElements());
    }



}
