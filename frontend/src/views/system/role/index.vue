<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createRole,
  deleteRole,
  pageRoles,
  updateRole,
  type RoleForm,
  type SysRole
} from '@/api/role'

const loading = ref(false)
const data = ref<SysRole[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const editingId = ref<number | undefined>()
const formRef = ref<FormInstance>()
const form = reactive<RoleForm>({ name: '', code: '', remark: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
  code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const resp = await pageRoles(query)
    data.value = resp.records
    total.value = resp.total
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新建角色'
  editingId.value = undefined
  Object.assign(form, { name: '', code: '', remark: '' })
  dialogVisible.value = true
}

const openEdit = (row: SysRole) => {
  dialogTitle.value = '编辑角色'
  editingId.value = row.id
  Object.assign(form, { name: row.name, code: row.code, remark: row.remark ?? '' })
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (editingId.value) {
    await updateRole(editingId.value, form)
    ElMessage.success('更新成功')
  } else {
    await createRole(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  await load()
}

const remove = async (row: SysRole) => {
  await ElMessageBox.confirm(`确定删除角色 ${row.name}？`, '提示', { type: 'warning' })
  await deleteRole(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索角色名"
        clearable
        style="width: 240px"
        @keyup.enter="load"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button type="success" @click="openCreate">新建</el-button>
    </div>

    <el-table v-loading="loading" :data="data" border stripe style="margin-top: 12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="角色名" />
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="remark" label="备注" />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
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
