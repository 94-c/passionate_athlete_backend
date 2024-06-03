package com.backend.athlete.domain.user.dto.request;

import com.backend.athlete.domain.user.model.type.UserStatusType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateUserStatusRequest {

    @NotNull(message = "회원 상태를 입력 해주세요.")
    private UserStatusType status;

}
