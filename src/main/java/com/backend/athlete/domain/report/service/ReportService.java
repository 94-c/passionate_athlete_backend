package com.backend.athlete.domain.report.service;

import com.backend.athlete.domain.athlete.model.Athlete;
import com.backend.athlete.domain.athlete.model.type.AthleteSuccessType;
import com.backend.athlete.domain.athlete.repository.AthleteRepository;
import com.backend.athlete.domain.attendance.model.Attendance;
import com.backend.athlete.domain.attendance.repository.AttendanceRepository;
import com.backend.athlete.domain.report.dto.response.GetWeeklyAttendanceResponse;
import com.backend.athlete.domain.user.model.User;
import com.backend.athlete.domain.user.repository.UserRepository;
import com.backend.athlete.global.jwt.service.CustomUserDetailsImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final AttendanceRepository attendanceRepository;
    private final AthleteRepository athleteRepository;
    private final UserRepository userRepository;

    public ReportService(AttendanceRepository attendanceRepository, AthleteRepository athleteRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.athleteRepository = athleteRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public GetWeeklyAttendanceResponse getWeeklyAttendance(CustomUserDetailsImpl userPrincipal, LocalDate week) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());
        LocalDate startOfWeek = week.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = week.with(DayOfWeek.SUNDAY);

        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateBetween(findUser, startOfWeek, endOfWeek);
        List<Athlete> athletes = athleteRepository.findByUserAndDailyTimeBetween(findUser, startOfWeek, endOfWeek);

        List<LocalDate> presentDays = attendances.stream()
                .map(Attendance::getAttendanceDate)
                .collect(Collectors.toList());

        List<LocalDate> successDays = athletes.stream()
                .filter(athlete -> athlete.getType() == AthleteSuccessType.SUCCESS)
                .map(Athlete::getDailyTime)
                .collect(Collectors.toList());

        List<LocalDate> failDays = athletes.stream()
                .filter(athlete -> athlete.getType() == AthleteSuccessType.FAIL)
                .map(Athlete::getDailyTime)
                .collect(Collectors.toList());

        int totalDaysPresent = presentDays.size();
        int totalDaysSuccess = successDays.size();
        int totalDaysFail = failDays.size();

        return GetWeeklyAttendanceResponse.fromEntity(totalDaysPresent, totalDaysSuccess, totalDaysFail, presentDays, successDays, failDays);
    }

    @Transactional
    public GetWeeklyAttendanceResponse getMonthlyAttendance(CustomUserDetailsImpl userPrincipal, YearMonth month) {
        User findUser = userRepository.findByUserId(userPrincipal.getUsername());
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<Attendance> attendances = attendanceRepository.findByUserAndAttendanceDateBetween(findUser, startDate, endDate);
        List<Athlete> athletes = athleteRepository.findByUserAndDailyTimeBetween(findUser, startDate, endDate);

        List<LocalDate> presentDays = attendances.stream()
                .map(Attendance::getAttendanceDate)
                .collect(Collectors.toList());

        List<LocalDate> successDays = athletes.stream()
                .filter(athlete -> athlete.getType() == AthleteSuccessType.SUCCESS)
                .map(Athlete::getDailyTime)
                .collect(Collectors.toList());

        List<LocalDate> failDays = athletes.stream()
                .filter(athlete -> athlete.getType() == AthleteSuccessType.FAIL)
                .map(Athlete::getDailyTime)
                .collect(Collectors.toList());

        int totalDaysPresent = presentDays.size();
        int totalDaysSuccess = successDays.size();
        int totalDaysFail = failDays.size();

        return GetWeeklyAttendanceResponse.fromEntity(totalDaysPresent, totalDaysSuccess, totalDaysFail, presentDays, successDays, failDays);
    }

}
