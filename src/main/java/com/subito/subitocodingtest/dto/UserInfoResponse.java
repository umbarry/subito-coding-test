package com.subito.subitocodingtest.dto;

import com.subito.subitocodingtest.model.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public static UserInfoResponse fromUserInfo(UserInfo userInfo) {
        return UserInfoResponse.builder()
                .id(userInfo.getId())
                .firstName(userInfo.getFirstName())
                .lastName(userInfo.getLastName())
                .email(userInfo.getEmail())
                .phoneNumber(userInfo.getPhoneNumber())
                .build();
    }
}


