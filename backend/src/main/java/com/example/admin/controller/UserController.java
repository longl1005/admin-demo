package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.BusinessException;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.UserSaveDTO;
import com.example.admin.entity.SysUser;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.mapper.UserRoleMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final SysUserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(keyword), SysUser::getUsername, keyword)
                .orderByDesc(SysUser::getId);
        Page<SysUser> p = userMapper.selectPage(new Page<>(page, size), qw);
        p.getRecords().forEach(u -> u.setPassword(null));
        return Result.ok(PageResult.from(p));
    }

    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        SysUser u = userMapper.selectById(id);
        if (u == null) throw new BusinessException(404, "用户不存在");
        u.setPassword(null);
        return Result.ok(u);
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getRoleIds(@PathVariable Long id) {
        return Result.ok(userRoleMapper.findRoleIdsByUserId(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserSaveDTO dto) {
        if (!StringUtils.hasText(dto.getPassword())) {
            throw new BusinessException(400, "新建用户必须提供密码");
        }
        SysUser exists = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (exists != null) throw new BusinessException(409, "用户名已存在");

        SysUser u = new SysUser();
        u.setUsername(dto.getUsername());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setNickname(dto.getNickname());
        u.setEmail(dto.getEmail());
        u.setAvatar(dto.getAvatar());
        u.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        userMapper.insert(u);

        replaceRoles(u.getId(), dto.getRoleIds());
        return Result.ok(u.getId());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserSaveDTO dto) {
        SysUser existing = userMapper.selectById(id);
        if (existing == null) throw new BusinessException(404, "用户不存在");

        existing.setNickname(dto.getNickname());
        existing.setEmail(dto.getEmail());
        existing.setAvatar(dto.getAvatar());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        if (StringUtils.hasText(dto.getPassword())) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(existing);

        if (dto.getRoleIds() != null) {
            replaceRoles(id, dto.getRoleIds());
        }
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (id == 1L) throw new BusinessException(400, "不能删除超级管理员");
        userMapper.deleteById(id);
        userRoleMapper.deleteByUserId(id);
        return Result.ok();
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.deleteByUserId(userId);
        if (roleIds != null) {
            for (Long rid : roleIds) {
                userRoleMapper.insert(userId, rid);
            }
        }
    }
}
