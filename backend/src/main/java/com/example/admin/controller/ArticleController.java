package com.example.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.BusinessException;
import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.entity.Article;
import com.example.admin.mapper.ArticleMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.example.admin.security.LoginUser;

@RestController
@RequestMapping("/content/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleMapper articleMapper;

    @GetMapping
    public Result<PageResult<Article>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Article> qw = new LambdaQueryWrapper<Article>()
                .like(StringUtils.hasText(keyword), Article::getTitle, keyword)
                .eq(status != null, Article::getStatus, status)
                .orderByDesc(Article::getId);
        Page<Article> p = articleMapper.selectPage(new Page<>(page, size), qw);
        return Result.ok(PageResult.from(p));
    }

    @GetMapping("/{id}")
    public Result<Article> get(@PathVariable Long id) {
        Article a = articleMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "文章不存在");
        return Result.ok(a);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> create(@Valid @RequestBody ArticleForm form,
                               @AuthenticationPrincipal LoginUser loginUser) {
        Article a = new Article();
        a.setTitle(form.getTitle());
        a.setSummary(form.getSummary());
        a.setContent(form.getContent());
        a.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        a.setAuthor(loginUser != null ? loginUser.getUsername() : "system");
        articleMapper.insert(a);
        return Result.ok(a.getId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ArticleForm form) {
        Article a = articleMapper.selectById(id);
        if (a == null) throw new BusinessException(404, "文章不存在");
        a.setTitle(form.getTitle());
        a.setSummary(form.getSummary());
        a.setContent(form.getContent());
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
    public static class ArticleForm {
        @NotBlank private String title;
        private String summary;
        private String content;
        private Integer status;
    }
}
