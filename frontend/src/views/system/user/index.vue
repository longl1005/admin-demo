<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createUser,
  deleteUser,
  getUserRoleIds,
  pageUsers,
  updateUser,
  type SysUser,
  type UserSaveDTO
} from '@/api/user'
import { listAllRoles, type SysRole } from '@/api/role'

const loading = ref(false)
const data = ref<SysUser[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '' })
const roles = ref<SysRole[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive<UserSaveDTO>({
  id: undefined,
  username: '',
  password: '',
  nickname: '',
  email: '',
  status: 1,
  roleIds: []
})
const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }]
}

const loadRoles = async () => {
  roles.value = await listAllRoles()
}

const load = async () => {
  loading.value = true
  try {
    const resp = await pageUsers(query)
    data.value = resp.records
    total.value = resp.total
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新建用户'
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    nickname: '',
    email: '',
    status: 1,
    roleIds: []
  })
  dialogVisible.value = true
}

const openEdit = async (row: SysUser) => {
  dialogTitle.value = '编辑用户'
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    nickname: row.nickname ?? '',
    email: row.email ?? '',
    status: row.status,
    roleIds: []
  })
  form.roleIds = await getUserRoleIds(row.id)
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (form.id) {
    await updateUser(form.id, form)
    ElMessage.success('更新成功')
  } else {
    await createUser(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  await load()
}

const remove = async (row: SysUser) => {
  await ElMessageBox.confirm(`确定删除用户 ${row.username}？`, '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  await load()
}

const search = () => {
  query.page = 1
  load()
}

onMounted(() => {
  loadRoles()
  load()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索用户名"
        clearable
        style="width: 240px"
        @keyup.enter="search"
      />
      <el-button type="primary" @click="search"><el-icon><Search /></el-icon>查询</el-button>
      <div class="spacer" />
      <el-button type="success" @click="openCreate">
        <el-icon><Plus /></el-icon>新建
      </el-button>
    </div>

    <el-table v-loading="loading" :data="data" border stripe style="margin-top: 12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" :disabled="row.id === 1" @click="remove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      style="margin-top: 12px; justify-content: flex-end"
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @current-change="load"
      @size-change="load"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="form.id ? '留空则不修改' : '必填'"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.name" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
.spacer {
  flex: 1;
}
</style>
