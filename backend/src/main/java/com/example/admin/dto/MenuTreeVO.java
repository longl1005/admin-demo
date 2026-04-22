package com.example.admin.dto;

import com.example.admin.entity.SysMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MenuTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Integer type;
    private String permission;
    private List<MenuTreeVO> children = new ArrayList<>();

    public static MenuTreeVO from(SysMenu m) {
        MenuTreeVO v = new MenuTreeVO();
        v.setId(m.getId());
        v.setParentId(m.getParentId());
        v.setName(m.getName());
        v.setPath(m.getPath());
        v.setComponent(m.getComponent());
        v.setIcon(m.getIcon());
        v.setSort(m.getSort());
        v.setType(m.getType());
        v.setPermission(m.getPermission());
        return v;
    }
}
