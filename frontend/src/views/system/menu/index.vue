<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { MenuItem } from '@/api/auth'
import { fetchMenuTree } from '@/api/menu'

const loading = ref(false)
const tree = ref<MenuItem[]>([])

const load = async () => {
  loading.value = true
  try {
    tree.value = await fetchMenuTree()
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <template #header>
      <span>菜单列表（只读示例）</span>
    </template>
    <el-table
      v-loading="loading"
      :data="tree"
      row-key="id"
      :tree-props="{ children: 'children' }"
      border
      stripe
      default-expand-all
    >
      <el-table-column prop="name" label="名称" width="220" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="component" label="组件" />
      <el-table-column prop="icon" label="图标" width="120">
        <template #default="{ row }">
          <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="permission" label="权限标识" />
      <el-table-column prop="sort" label="排序" width="80" />
    </el-table>
  </el-card>
</template>
