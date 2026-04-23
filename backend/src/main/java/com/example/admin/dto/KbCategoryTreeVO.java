package com.example.admin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KbCategoryTreeVO {
    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private Integer sort;
    private List<KbCategoryTreeVO> children = new ArrayList<>();
}
