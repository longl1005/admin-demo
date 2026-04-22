<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import type { MenuItem } from '@/api/auth'
import { ElMessageBox } from 'element-plus'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const collapsed = ref(false)

onMounted(async () => {
  if (!auth.userInfo) {
    await auth.loadUserInfo()
  }
})

const activeMenu = computed(() => route.path)

const topMenus = computed<MenuItem[]>(() => auth.menus as MenuItem[])

const handleLogout = async () => {
  await ElMessageBox.confirm('确定退出登录？', '提示', { type: 'warning' })
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span v-if="!collapsed">Admin Demo</span>
        <span v-else>AD</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        background-color="#001529"
        text-color="#c9d1d9"
        active-text-color="#ffffff"
        router
      >
        <template v-for="menu in topMenus" :key="menu.id">
          <el-sub-menu v-if="menu.children && menu.children.length" :index="'g-' + menu.id">
            <template #title>
              <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.id"
              :index="child.path"
            >
              <el-icon v-if="child.icon"><component :is="child.icon" /></el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="menu.icon"><component :is="menu.icon" /></el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-button link @click="collapsed = !collapsed">
          <el-icon :size="20">
            <component :is="collapsed ? 'Expand' : 'Fold'" />
          </el-icon>
        </el-button>
        <div class="spacer" />
        <el-dropdown @command="(cmd: string) => cmd === 'logout' && handleLogout()">
          <span class="user">
            <el-avatar :size="28" :src="auth.userInfo?.avatar">
              {{ auth.userInfo?.nickname?.[0] ?? auth.userInfo?.username?.[0] ?? 'U' }}
            </el-avatar>
            <span class="name">{{ auth.userInfo?.nickname || auth.userInfo?.username }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item disabled>
                角色: {{ auth.roles.join(', ') || '-' }}
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background-color: #001529;
  transition: width 0.2s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  letter-spacing: 1px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.header {
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 16px;
}
.spacer {
  flex: 1;
}
.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.user .name {
  font-size: 14px;
  color: #303133;
}
.main {
  background-color: #f5f7fa;
  padding: 16px;
}
:deep(.el-menu) {
  border-right: none;
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.15s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
