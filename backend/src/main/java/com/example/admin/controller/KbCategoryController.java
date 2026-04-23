package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.admin.common.BusinessException;
import com.example.admin.common.Result;
import com.example.admin.dto.KbCategoryTreeVO;
import com.example.admin.entity.KbArticle;
import com.example.admin.entity.KbCategory;
import com.example.admin.mapper.KbArticleMapper;
import com.example.admin.mapper.KbCategoryMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/content/kb/categories")
@RequiredArgsConstructor
public class KbCategoryController {

    private final KbCategoryMapper categoryMapper;
    private final KbArticleMapper articleMapper;

    @GetMapping
    public Result<List<KbCategory>> list() {
        List<KbCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<KbCategory>()
                        .orderByAsc(KbCategory::getSort)
                        .orderByAsc(KbCategory::getId));
        return Result.ok(all);
    }

    @GetMapping("/tree")
    public Result<List<KbCategoryTreeVO>> tree() {
        List<KbCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<KbCategory>()
                        .orderByAsc(KbCategory::getSort)
                        .orderByAsc(KbCategory::getId));
        Map<Long, KbCategoryTreeVO> index = new HashMap<>();
        for (KbCategory c : all) {
            KbCategoryTreeVO vo = new KbCategoryTreeVO();
            vo.setId(c.getId());
            vo.setParentId(c.getParentId());
            vo.setName(c.getName());
            vo.setDescription(c.getDescription());
            vo.setSort(c.getSort());
            index.put(c.getId(), vo);
        }
        List<KbCategoryTreeVO> roots = new ArrayList<>();
        for (KbCategoryTreeVO vo : index.values()) {
            Long pid = vo.getParentId() == null ? 0L : vo.getParentId();
            if (pid == 0L || !index.containsKey(pid)) {
                roots.add(vo);
            } else {
                index.get(pid).getChildren().add(vo);
            }
        }
        Comparator<KbCategoryTreeVO> cmp = Comparator
                .comparing((KbCategoryTreeVO v) -> v.getSort() == null ? 0 : v.getSort())
                .thenComparing(KbCategoryTreeVO::getId);
        sortRecursively(roots, cmp);
        return Result.ok(roots);
    }

    private void sortRecursively(List<KbCategoryTreeVO> list, Comparator<KbCategoryTreeVO> cmp) {
        list.sort(cmp);
        for (KbCategoryTreeVO v : list) sortRecursively(v.getChildren(), cmp);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody CategoryForm form) {
        KbCategory c = new KbCategory();
        c.setName(form.getName());
        c.setDescription(form.getDescription());
        c.setParentId(form.getParentId() == null ? 0L : form.getParentId());
        c.setSort(form.getSort() == null ? 0 : form.getSort());
        categoryMapper.insert(c);
        return Result.ok(c.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CategoryForm form) {
        KbCategory c = categoryMapper.selectById(id);
        if (c == null) throw new BusinessException(404, "分类不存在");
        if (form.getParentId() != null && form.getParentId().equals(id)) {
            throw new BusinessException(400, "父级分类不能是自身");
        }
        c.setName(form.getName());
        c.setDescription(form.getDescription());
        if (form.getParentId() != null) c.setParentId(form.getParentId());
        if (form.getSort() != null) c.setSort(form.getSort());
        categoryMapper.updateById(c);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<KbCategory>().eq(KbCategory::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(400, "存在子分类，无法删除");
        }
        Long articleCount = articleMapper.selectCount(
                new LambdaQueryWrapper<KbArticle>().eq(KbArticle::getCategoryId, id));
        if (articleCount != null && articleCount > 0) {
            throw new BusinessException(400, "该分类下仍有知识条目，无法删除");
        }
        categoryMapper.deleteById(id);
        return Result.ok();
    }

    @Data
    public static class CategoryForm {
        @NotBlank
        private String name;
        private String description;
        private Long parentId;
        private Integer sort;
    }
}
