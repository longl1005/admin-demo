<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createKbCategory,
  deleteKbCategory,
  fetchKbCategoryTree,
  updateKbCategory,
  type KbCategoryForm,
  type KbCategoryTree
} from '@/api/kb'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const canWrite = () => auth.roles.includes('ROLE_ADMIN')

const loading = ref(false)
const treeData = ref<KbCategoryTree[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const editingId = ref<number | undefined>()
const formRef = ref<FormInstance>()
const form = reactive<KbCategoryForm>({
  name: '',
  description: '',
  parentId: 0,
  sort: 0
})
const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const flatten = (nodes: KbCategoryTree[], out: KbCategoryTree[] = []) => {
  for (const n of nodes) {
    out.push(n)
    if (n.children?.length) flatten(n.children, out)
  }
  return out
}

const parentOptions = ref<{ label: string; value: number }[]>([])

const load = async () => {
  loading.value = true
  try {
    const tree = await fetchKbCategoryTree()
    treeData.value = tree
    parentOptions.value = [
      { label: '（顶级）', value: 0 },
      ...flatten(tree).map((n) => ({ label: n.name, value: n.id }))
    ]
  } finally {
    loading.value = false
  }
}

const openCreate = (parentId = 0) => {
  dialogTitle.value = '新建分类'
  editingId.value = undefined
  Object.assign(form, { name: '', description: '', parentId, sort: 0 })
  dialogVisible.value = true
}

const openEdit = (row: KbCategoryTree) => {
  dialogTitle.value = '编辑分类'
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    description: row.description ?? '',
    parentId: row.parentId ?? 0,
    sort: row.sort ?? 0
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (editingId.value) {
    await updateKbCategory(editingId.value, form)
    ElMessage.success('更新成功')
  } else {
    await createKbCategory(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  await load()
}

const remove = async (row: KbCategoryTree) => {
  await ElMessageBox.confirm(`确定删除分类《${row.name}》？`, '提示', { type: 'warning' })
  await deleteKbCategory(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span style="color: #606266">知识库分类，支持两级结构。</span>
      <div class="spacer" />
      <el-button v-if="canWrite()" type="success" @click="openCreate(0)">新建顶级分类</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="treeData"
      row-key="id"
      :tree-props="{ children: 'children' }"
      default-expand-all
      border
      stripe
      style="margin-top: 12px"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="description" label="描述" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column v-if="canWrite()" label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openCreate(row.id)">新建子分类</el-button>
          <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="上级分类">
          <el-select v-model="form.parentId" style="width: 100%">
            <el-option
              v-for="opt in parentOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
              :disabled="editingId !== undefined && opt.value === editingId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
