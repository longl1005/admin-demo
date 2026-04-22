package com.example.admin.service.impl;

import com.example.admin.common.BusinessException;
import com.example.admin.dto.LoginRequest;
import com.example.admin.dto.LoginResponse;
import com.example.admin.dto.UserInfoVO;
import com.example.admin.entity.SysMenu;
import com.example.admin.entity.SysRole;
import com.example.admin.mapper.SysMenuMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.security.JwtUtil;
import com.example.admin.security.LoginUser;
import com.example.admin.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SysUserMapper userMapper;
    private final SysMenuMapper menuMapper;

    @Value("${app.jwt.expire-minutes:120}")
    private long expireMinutes;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUser principal = (LoginUser) auth.getPrincipal();
        String token = jwtUtil.generateToken(principal.getUserId(), principal.getUsername());
        return new LoginResponse(token, "Bearer", expireMinutes);
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser lu)) {
            throw new BusinessException(401, "未登录");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(lu.getUserId());
        vo.setUsername(lu.getUsername());
        vo.setNickname(lu.getUser().getNickname());
        vo.setEmail(lu.getUser().getEmail());
        vo.setAvatar(lu.getUser().getAvatar());

        List<SysRole> roles = userMapper.findRolesByUserId(lu.getUserId());
        vo.setRoles(roles.stream().map(SysRole::getCode).toList());

        List<SysMenu> menus = menuMapper.findMenusByUserId(lu.getUserId());
        vo.setPermissions(menus.stream()
                .map(SysMenu::getPermission)
                .filter(StringUtils::hasText)
                .distinct()
                .toList());
        return vo;
    }
}
