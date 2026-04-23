<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { pageKbArticles } from '@/api/kb'
import { pageArticles } from '@/api/article'

const auth = useAuthStore()
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '凌晨好'
  if (h < 12) return '早上好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const articleCount = ref(0)
const kbCount = ref(0)

const cards = computed(() => [
  { title: '用户数', value: 2, color: '#409EFF', icon: 'User' },
  { title: '角色数', value: 2, color: '#67C23A', icon: 'UserFilled' },
  { title: '文章数', value: articleCount.value, color: '#F56C6C', icon: 'EditPen' },
  { title: '知识条目数', value: kbCount.value, color: '#909399', icon: 'Reading' }
])

onMounted(async () => {
  try {
    const [a, k] = await Promise.all([
      pageArticles({ page: 1, size: 1 }),
      pageKbArticles({ page: 1, size: 1 })
    ])
    articleCount.value = a.total
    kbCount.value = k.total
  } catch {
    // ignore; role may not have access
  }
})
</script>

<template>
  <div>
    <el-card shadow="never">
      <div class="hero">
        <div>
          <h2 style="margin: 0">
            {{ greeting }}，{{ auth.userInfo?.nickname || auth.userInfo?.username }} 👋
          </h2>
          <p style="margin: 6px 0 0; color: #909399">
            当前角色：{{ auth.roles.join('、') || '-' }}
          </p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col v-for="c in cards" :key="c.title" :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat">
            <div class="icon" :style="{ backgroundColor: c.color }">
              <el-icon :size="24"><component :is="c.icon" /></el-icon>
            </div>
            <div>
              <div class="label">{{ c.title }}</div>
              <div class="value">{{ c.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 16px">
      <template #header>快速开始</template>
      <ul style="line-height: 2; color: #606266">
        <li>左侧菜单根据登录用户的角色动态加载（RBAC）。</li>
        <li>后端 API 前缀：<code>/api</code>，JWT 存储于 <code>localStorage.token</code>。</li>
        <li>默认账号：<code>admin / admin123</code>（管理员），<code>user / user123</code>（只读）。</li>
        <li>示例 CRUD：查看 <b>内容管理 → 文章管理</b>。</li>
        <li>知识库：查看 <b>内容管理 → 知识库</b>，支持 Markdown 与分类筛选。</li>
      </ul>
    </el-card>
  </div>
</template>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.stat-card {
  border-radius: 6px;
}
.stat {
  display: flex;
  align-items: center;
  gap: 16px;
}
.icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.label {
  color: #909399;
  font-size: 13px;
}
.value {
  font-size: 22px;
  font-weight: 600;
}
</style>
