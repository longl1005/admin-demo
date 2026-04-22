package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.BusinessException;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.entity.SysRole;
import com.example.admin.mapper.SysRoleMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final SysRoleMapper roleMapper;

    @GetMapping
    public Result<PageResult<SysRole>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysRole> qw = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(keyword), SysRole::getName, keyword)
                .orderByDesc(SysRole::getId);
        Page<SysRole> p = roleMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(PageResult.from(p));
    }

    @GetMapping("/all")
    public Result<List<SysRole>> all() {
        return Result.ok(roleMapper.selectList(null));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleForm form) {
        SysRole exists = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, form.getCode()));
        if (exists != null) throw new BusinessException(409, "角色编码已存在");
        SysRole r = new SysRole();
        r.setName(form.getName());
        r.setCode(form.getCode());
        r.setRemark(form.getRemark());
        roleMapper.insert(r);
        return Result.ok(r.getId());
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleForm form) {
        SysRole r = roleMapper.selectById(id);
        if (r == null) throw new BusinessException(404, "角色不存在");
        r.setName(form.getName());
        r.setRemark(form.getRemark());
        roleMapper.updateById(r);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.ok();
    }

    @Data
    public static class RoleForm {
        @NotBlank private String name;
        @NotBlank private String code;
        private String remark;
    }
}
