package com.backend.athlete.application;

import com.backend.athlete.domain.attendance.Attendance;
import com.backend.athlete.domain.attendance.AttendanceRepository;
import com.backend.athlete.domain.user.User;
import com.backend.athlete.presentation.attendance.request.CreateAttendanceRequest;
import com.backend.athlete.presentation.attendance.response.CreateAttendanceResponse;
import com.backend.athlete.presentation.attendance.response.GetContinuousAttendanceResponse;
import com.backend.athlete.presentation.attendance.response.GetDailyAttendanceResponse;
import com.backend.athlete.presentation.attendance.response.GetMonthlyAttendanceResponse;
import com.backend.athlete.support.exception.ServiceException;
import com.backend.athlete.support.jwt.service.CustomUserDetailsImpl;
import com.backend.athlete.support.util.FindUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public CreateAttendanceResponse createAttendanceEvent(CustomUserDetailsImpl userPrincipal, CreateAttendanceRequest request) {
        User user = FindUtils.findByUserId(userPrincipal.getUsername());

        if (request.getEventDate() == null) request.setEventDate(LocalDate.now());

        LocalDate eventDate = request.getEventDate();

        if (attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), eventDate).isPresent()) {
            throw new ServiceException("이미 해당 " +  eventDate + " 일에 출석 했습니다. ");
        }

        Attendance attendance = attendanceRepository.save(CreateAttendanceRequest.toEntity(request, user));

        long totalAttendanceCount = attendanceRepository.countByUserId(user.getId());

        return CreateAttendanceResponse.fromEntity(attendance, totalAttendanceCount);
    }

    @Transactional
    public GetDailyAttendanceResponse getAttendance(CustomUserDetailsImpl userPrincipal, LocalDate dailyDate) {
        User user = FindUtils.findByUserId(userPrincipal.getUsername());

        Attendance attendance = attendanceRepository.findByUserIdAndAttendanceDate(user.getId(), dailyDate)
                .orElseThrow(() -> new ServiceException(dailyDate + " 의 출석이 없습니다."));


        return GetDailyAttendanceResponse.fromEntity(attendance);
    }

    @Transactional
    public GetMonthlyAttendanceResponse getMonthlyAttendance(CustomUserDetailsImpl userPrincipal, YearMonth month) {
        User user = FindUtils.findByUserId(userPrincipal.getUsername());

        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateBetween(user, startDate, endDate);

        List<LocalDate> presentDays = attendances.stream()
                .map(Attendance::getAttendanceDate)
                .collect(Collectors.toList());

        return new GetMonthlyAttendanceResponse(presentDays.size(), presentDays);
    }


    public GetContinuousAttendanceResponse continuousAttendance(CustomUserDetailsImpl userPrincipal) {
        User user = FindUtils.findByUserId(userPrincipal.getUsername());

        LocalDate currentDate = LocalDate.now();
        int continuousAttendanceCount = calculateContinuousAttendance(user.getId(), currentDate);

        return new GetContinuousAttendanceResponse(user.getId(), continuousAttendanceCount);
    }

    private int calculateContinuousAttendance(Long userId, LocalDate currentDate) {
        List<Attendance> attendances = attendanceRepository.findAllByUserIdOrderByAttendanceDateAsc(userId);

        int continuousAttendanceCount = 0;
        int maxContinuousDays = 0;
        LocalDate previousDate = null;

        for (Attendance attendance : attendances) {
            LocalDate attendanceDate = attendance.getAttendanceDate();

            // 주말 (토요일과 일요일)은 연속 출석일에서 제외
            if (attendanceDate.getDayOfWeek() == DayOfWeek.SATURDAY || attendanceDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            // 첫 출석일 초기화
            if (previousDate == null) {
                continuousAttendanceCount = 1;
            } else {
                // 이전 출석일로부터 하루 이후에 출석한 경우
                if (attendanceDate.equals(previousDate.plusDays(1))) {
                    continuousAttendanceCount++;
                } else {
                    // 이전 출석일로부터 연속 출석이 끊긴 경우
                    if (previousDate.getDayOfWeek() == DayOfWeek.MONDAY) {
                        continuousAttendanceCount = 0; // 월요일에 출석이 끊기면 연속 출석 초기화
                    } else {
                        continuousAttendanceCount = 1; // 그 외의 경우 연속 출석 초기화
                    }
                }
            }

            // 연속 출석 최대 일 수 업데이트
            maxContinuousDays = Math.max(maxContinuousDays, continuousAttendanceCount);
            previousDate = attendanceDate;
        }

        // 현재 날짜가 주말인 경우 연속 출석일 수를 0으로 처리
        if (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY || currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            maxContinuousDays = 0;
        }

        return maxContinuousDays;
    }




}
