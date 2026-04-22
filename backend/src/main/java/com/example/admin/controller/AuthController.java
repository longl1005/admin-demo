package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.LoginRequest;
import com.example.admin.dto.LoginResponse;
import com.example.admin.dto.MenuTreeVO;
import com.example.admin.dto.UserInfoVO;
import com.example.admin.entity.SysMenu;
import com.example.admin.mapper.SysMenuMapper;
import com.example.admin.security.LoginUser;
import com.example.admin.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysMenuMapper menuMapper;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        // Stateless JWT: client simply drops the token. Endpoint exists for symmetry.
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<UserInfoVO> me() {
        return Result.ok(authService.getCurrentUserInfo());
    }

    @GetMapping("/menus")
    public Result<List<MenuTreeVO>> menus(@AuthenticationPrincipal LoginUser loginUser) {
        List<SysMenu> menus = menuMapper.findMenusByUserId(loginUser.getUserId());
        return Result.ok(buildTree(menus));
    }

    private List<MenuTreeVO> buildTree(List<SysMenu> menus) {
        Map<Long, MenuTreeVO> map = menus.stream()
                .collect(Collectors.toMap(SysMenu::getId, MenuTreeVO::from, (a, b) -> a, LinkedHashMap::new));
        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO v : map.values()) {
            if (v.getParentId() == null || v.getParentId() == 0L) {
                roots.add(v);
            } else {
                MenuTreeVO parent = map.get(v.getParentId());
                if (parent != null) {
                    parent.getChildren().add(v);
                } else {
                    roots.add(v);
                }
            }
        }
        return roots;
    }
}
