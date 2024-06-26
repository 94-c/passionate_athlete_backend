package com.backend.athlete.domain.user.data;

import com.backend.athlete.domain.user.User;
import com.backend.athlete.domain.user.type.UserGenderType;
import com.backend.athlete.domain.user.type.UserStatusType;
import lombok.Getter;

@Getter
public class UserDetailsInfo {
    private Long id;
    private String code;
    private String userId;
    private String name;
    private String password;
    private UserStatusType status;
    private UserGenderType gender;
    private String branchName;

    public UserDetailsInfo(Long id, String code, String userId, String name, String password, UserStatusType status, UserGenderType gender, String branchName) {
        this.id = id;
        this.code = code;
        this.userId = userId;
        this.name = name;
        this.password = password;
        this.status = status;
        this.gender = gender;
        this.branchName = branchName;
    }

    public static UserDetailsInfo from(User user) {
        return new UserDetailsInfo(
                user.getId(),
                user.getCode(),
                user.getUserId(),
                user.getName(),
                user.getPassword(),
                user.getStatus(),
                user.getGender(),
                user.getBranch() != null ? user.getBranch().getName() : null
        );
    }
}
