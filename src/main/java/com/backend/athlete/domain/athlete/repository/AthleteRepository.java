package com.backend.athlete.domain.athlete.repository;

import com.backend.athlete.domain.athlete.dto.GroupedAthleteRecordDto;
import com.backend.athlete.domain.athlete.model.Athlete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    @Query("SELECT a FROM Athlete a WHERE a.user.id = :userId AND a.dailyTime = :dailyTime")
    List<Athlete> findAthletesByUserIdAndDailyTime(@Param("userId") Long userId, @Param("dailyTime") LocalDate dailyTime);

    @Query("SELECT new com.backend.athlete.domain.athlete.dto.GroupedAthleteRecordDto(a.dailyTime, a.athletics, a.type, SUM(a.record) as totalRecord, COUNT(a.id) as recordCount, a.etc, a.user.name as username) " +
            "FROM Athlete a " +
            "WHERE a.user.id = :userId AND a.dailyTime BETWEEN :startDate AND :endDate " +
            "GROUP BY a.dailyTime, a.athletics, a.type, a.etc, a.user.name")
    List<GroupedAthleteRecordDto> findGroupedAthletesByUserIdAndYearMonth(@Param("userId") Long userId,
                                                                          @Param("startDate") LocalDate startDate,
                                                                          @Param("endDate") LocalDate endDate);

}