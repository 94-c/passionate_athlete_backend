package com.backend.athlete.domain.athlete.service;

import com.backend.athlete.domain.athlete.dto.data.AthleteData;
import com.backend.athlete.domain.athlete.dto.request.CreateAthleteRequest;
import com.backend.athlete.domain.athlete.dto.response.CreateAthleteResponse;
import com.backend.athlete.domain.athlete.dto.response.GetDailyAthleteResponse;
import com.backend.athlete.domain.athlete.dto.response.GetMonthlyAthleteResponse;
import com.backend.athlete.domain.athlete.model.Athlete;
import com.backend.athlete.domain.athlete.repository.AthleteRepository;
import com.backend.athlete.domain.user.model.User;
import com.backend.athlete.domain.user.repository.UserRepository;
import com.backend.athlete.global.jwt.service.CustomUserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final UserRepository userRepository;

    public AthleteService(AthleteRepository athleteRepository, UserRepository userRepository) {
        this.athleteRepository = athleteRepository;
        this.userRepository = userRepository;
    }

    /**
     * 데일리 운동 기록 저장
     */
    public CreateAthleteResponse createAthlete(CustomUserDetailsImpl userPrincipal, CreateAthleteRequest dto) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());
        if (findUser == null) {
            throw new UsernameNotFoundException("User not found");
        }
        dto.setDailyTime(LocalDate.now());
        Athlete createAthlete = athleteRepository.save(CreateAthleteRequest.toEntity(dto, findUser));

        return CreateAthleteResponse.fromEntity(createAthlete);
    }

    /**
     * 데일리 운동 기록 조회
     */
    @Transactional
    public GetDailyAthleteResponse getDailyAthlete(CustomUserDetailsImpl userPrincipal, LocalDate dailyDate) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());

        List<Athlete> athletes = athleteRepository.findAthletesByUserIdAndDailyTime(findUser.getId(), dailyDate);

        if (athletes.isEmpty()) {
            throw new EntityNotFoundException("No athlete found for the given user and date.");
        }

        Athlete athlete = athletes.get(0);

        return GetDailyAthleteResponse.fromEntity(athlete);
    }

    /**
     * 월별 운동 기록 조회
     */
    @Transactional
    public GetMonthlyAthleteResponse getMonthlyAthlete(CustomUserDetailsImpl userPrincipal, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        User findUser = userRepository.findByUserId(userPrincipal.getUsername());

        List<AthleteData> groupedAthleteRecords = athleteRepository.findGroupedAthletesByUserIdAndYearMonth(findUser.getId(), startDate, endDate);

        List<GetDailyAthleteResponse> monthlyRecords = groupedAthleteRecords.stream()
                .map(record -> new GetDailyAthleteResponse(
                        record.getDailyTime(),
                        record.getAthletics(),
                        record.getType().toString(),
                        record.getTotalRecordAsLocalTime(),
                        (int) record.getCount(),
                        record.getEtc(),
                        record.getUsername()
                ))
                .collect(Collectors.toList());

        return GetMonthlyAthleteResponse.fromEntity(yearMonth, monthlyRecords);
    }

    public void deleteDailyAthleteById(CustomUserDetailsImpl userPrincipal, Long id) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());

        Athlete athlete = athleteRepository.findByIdAndUserId(id, findUser.getId())
                .orElseThrow(() -> new RuntimeException("Athlete record not found or not authorized"));

        athleteRepository.delete(athlete);
    }
}
