package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.BusinessException;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.entity.KbArticle;
import com.example.admin.mapper.KbArticleMapper;
import com.example.admin.security.LoginUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/content/kb/articles")
@RequiredArgsConstructor
public class KbArticleController {

    private final KbArticleMapper articleMapper;

    @GetMapping
    public Result<PageResult<KbArticle>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<KbArticle> qw = new LambdaQueryWrapper<KbArticle>()
                .like(StringUtils.hasText(keyword), KbArticle::getTitle, keyword)
                .eq(categoryId != null, KbArticle::getCategoryId, categoryId)
                .eq(status != null, KbArticle::getStatus, status)
                .orderByDesc(KbArticle::getId);
        Page<KbArticle> p = articleMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(PageResult.from(p));
    }

    @GetMapping("/{id}")
    public Result<KbArticle> get(@PathVariable Long id) {
        KbArticle a = articleMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "知识条目不存在");
        Integer views = a.getViews() == null ? 0 : a.getViews();
        a.setViews(views + 1);
        articleMapper.updateById(a);
        return Result.ok(a);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody KbArticleForm form,
                               @AuthenticationPrincipal LoginUser loginUser) {
        KbArticle a = new KbArticle();
        a.setCategoryId(form.getCategoryId() == null ? 0L : form.getCategoryId());
        a.setTitle(form.getTitle());
        a.setSummary(form.getSummary());
        a.setContent(form.getContent());
        a.setTags(form.getTags());
        a.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        a.setAuthor(loginUser != null ? loginUser.getUsername() : "system");
        a.setViews(0);
        articleMapper.insert(a);
        return Result.ok(a.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KbArticleForm form) {
        KbArticle a = articleMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "知识条目不存在");
        a.setTitle(form.getTitle());
        a.setSummary(form.getSummary());
        a.setContent(form.getContent());
        a.setTags(form.getTags());
        if (form.getCategoryId() != null) a.setCategoryId(form.getCategoryId());
        if (form.getStatus() != null) a.setStatus(form.getStatus());
        articleMapper.updateById(a);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        articleMapper.deleteById(id);
        return Result.ok();
    }

    @Data
    public static class KbArticleForm {
        @NotBlank
        private String title;
        private String summary;
        private String content;
        private String tags;
        private Long categoryId;
        private Integer status;
    }
}
