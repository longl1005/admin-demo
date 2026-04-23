import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { public: true, title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'content/article',
        name: 'ContentArticle',
        component: () => import('@/views/content/article/index.vue'),
        meta: { title: '文章管理', icon: 'EditPen' }
      },
      {
        path: 'content/kb-category',
        name: 'ContentKbCategory',
        component: () => import('@/views/content/kb/category/index.vue'),
        meta: { title: '知识分类', icon: 'Folder' }
      },
      {
        path: 'content/kb',
        name: 'ContentKb',
        component: () => import('@/views/content/kb/article/index.vue'),
        meta: { title: '知识库', icon: 'Reading' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { public: true, title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.meta.public) return true
  if (!auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (!auth.userInfo) {
    try {
      await auth.loadUserInfo()
    } catch {
      auth.logout()
      return { path: '/login' }
    }
  }
  return true
})

router.afterEach((to) => {
  const title = (to.meta.title as string | undefined) ?? 'Admin Demo'
  document.title = `${title} · Admin Demo`
})

export default router
