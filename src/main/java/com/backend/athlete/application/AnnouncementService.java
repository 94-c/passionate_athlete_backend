package com.backend.athlete.application;

import com.backend.athlete.domain.announcement.Announcement;
import com.backend.athlete.domain.announcement.AnnouncementRepository;
import com.backend.athlete.domain.user.User;
import com.backend.athlete.domain.user.UserRepository;
import com.backend.athlete.presentation.request.CreateAnnouncementRequest;
import com.backend.athlete.presentation.request.PageSearchAnnouncementRequest;
import com.backend.athlete.presentation.request.UpdateAnnouncementRequest;
import com.backend.athlete.presentation.response.*;
import com.backend.athlete.support.exception.ServiceException;
import com.backend.athlete.support.jwt.service.CustomUserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final Path rootLocation = Paths.get("announcement-images");

    public AnnouncementService(AnnouncementRepository announcementRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
    }

    public Page<PageSearchAnnouncementResponse> searchAnnouncements(PageSearchAnnouncementRequest request, int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        String title = request.getTitle() != null ? request.getTitle() : "";
        String name = request.getName() != null ? request.getName() : "";

        Page<Announcement> announcements = announcementRepository.findAllByTitleContainingAndUserNameContainingAndStatusTrue(title, name, pageable);

        return announcements.map(PageSearchAnnouncementResponse::fromEntity);
    }


    public CreateAnnouncementResponse createAnnouncement(CustomUserDetailsImpl userPrincipal, CreateAnnouncementRequest request, MultipartFile file) throws IOException {
        User user = userRepository.findByUserId(userPrincipal.getUsername());

        if (!file.isEmpty()) {
            if (Files.notExists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), rootLocation.resolve(filename));
            request.setImagePath(rootLocation.resolve(filename).toString());
        }

        request.setStatus(true);
        Announcement createAnnouncement = announcementRepository.save(CreateAnnouncementRequest.toEntity(request, user));

        return CreateAnnouncementResponse.fromEntity(createAnnouncement);
    }

    public GetAnnouncementResponse getAnnouncement(Long id) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ServiceException("존재하지 않는 공지사항입니다."));

        if (!announcement.isStatus()) {
            throw new ServiceException("존재하지 않는 공지사항입니다.");
        }
        return GetAnnouncementResponse.fromEntity(announcement);
    }

    public UpdateAnnouncementResponse updateAnnouncement(Long id, CustomUserDetailsImpl userPrincipal, UpdateAnnouncementRequest request, MultipartFile file) throws IOException {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ServiceException("존재하지 않는 공지사항입니다."));

        if (!announcement.getUser().getUserId().equals(userPrincipal.getUsername())) {
            throw new ServiceException("공지사항의 권한이 존재하지 않습니다.");
        }

        String imagePath = announcement.getImagePath();
        if (!file.isEmpty()) {
            if (Files.notExists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), rootLocation.resolve(filename));
            imagePath = rootLocation.resolve(filename).toString();
        }

        announcement.update(
                request.getTitle(), request.getContent(), request.getImagePath()
        );

        Announcement updateAnnouncement = announcementRepository.save(announcement);

        return UpdateAnnouncementResponse.fromEntity(updateAnnouncement);
    }
    @Transactional
    public UpdateAnnouncementStatusResponse updateAnnouncementStatus(Long id, boolean status) {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ServiceException("존재하지 않는 공지사항입니다."));

        announcement.isStatus(status);
        announcementRepository.save(announcement);

        return UpdateAnnouncementStatusResponse.fromEntity(announcement);
    }

}