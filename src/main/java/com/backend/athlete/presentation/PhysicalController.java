package com.backend.athlete.presentation;

import com.backend.athlete.application.PhysicalService;
import com.backend.athlete.presentation.request.SavePhysicalRequest;
import com.backend.athlete.presentation.response.GetAllPhysicalResponse;
import com.backend.athlete.presentation.response.GetPhysicalResponse;
import com.backend.athlete.presentation.response.SavePhysicalResponse;
import com.backend.athlete.support.jwt.service.CustomUserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/physicals")
public class PhysicalController {

    private final PhysicalService physicalService;

    public PhysicalController(PhysicalService physicalService) {
        this.physicalService = physicalService;
    }

    /**
     * 1. 인바디 측정
     * 2. 당일 인바딩 조회
     * 3. 인바디 전체 조회 (페이징, 차감순)
     */
    @PostMapping
    public ResponseEntity<SavePhysicalResponse> savePhysical(@AuthenticationPrincipal CustomUserDetailsImpl userPrincipal,
                                                             @Valid @RequestBody SavePhysicalRequest request) {
        SavePhysicalResponse response = physicalService.savePhysical(userPrincipal, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/daily")
    public ResponseEntity<GetPhysicalResponse> getPhysical(@AuthenticationPrincipal CustomUserDetailsImpl userPrincipal,
                                                           @RequestParam(name = "daily", required = false) LocalDate dailyDate) {
        GetPhysicalResponse response = physicalService.getPhysical(userPrincipal, dailyDate);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<GetAllPhysicalResponse>> getAllPhysical(@AuthenticationPrincipal CustomUserDetailsImpl userPrincipal,
                                                                       @RequestParam(defaultValue = "0") int page,
                                                                       @RequestParam(defaultValue = "10") int size) {
        Page<GetAllPhysicalResponse> response = physicalService.getPhysicalData(userPrincipal, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
