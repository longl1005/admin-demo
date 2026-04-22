package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.MenuTreeVO;
import com.example.admin.entity.SysMenu;
import com.example.admin.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/system/menus")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MenuController {

    private final SysMenuMapper menuMapper;

    @GetMapping("/tree")
    public Result<List<MenuTreeVO>> tree() {
        List<SysMenu> all = menuMapper.selectList(null);
        all.sort(Comparator.comparing(SysMenu::getParentId).thenComparing(SysMenu::getSort));
        Map<Long, MenuTreeVO> map = all.stream()
                .collect(Collectors.toMap(SysMenu::getId, MenuTreeVO::from, (a, b) -> a, LinkedHashMap::new));
        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO v : map.values()) {
            if (v.getParentId() == null || v.getParentId() == 0L) {
                roots.add(v);
            } else {
                MenuTreeVO parent = map.get(v.getParentId());
                if (parent != null) parent.getChildren().add(v);
                else roots.add(v);
            }
        }
        return Result.ok(roots);
    }
}
