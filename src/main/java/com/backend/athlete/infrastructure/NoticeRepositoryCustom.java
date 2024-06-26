package com.backend.athlete.infrastructure;

import com.backend.athlete.domain.notice.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoticeRepositoryCustom {
    Page<Notice> findAllByUserAndTitleAndKindAndStatus(String name, String title, Pageable pageable, Integer kind, boolean Status);
}
