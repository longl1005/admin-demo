package com.example.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserSaveDTO {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度必须在 3 到 32 个字符之间")
    private String username;

    /** Required on create, optional on update. */
    private String password;

    private String nickname;
    private String email;
    private String avatar;
    private Integer status;
    private List<Long> roleIds;
}
