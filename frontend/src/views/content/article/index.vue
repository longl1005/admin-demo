<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createArticle,
  deleteArticle,
  pageArticles,
  updateArticle,
  type Article,
  type ArticleForm
} from '@/api/article'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const canWrite = () => auth.roles.includes('ROLE_ADMIN')

const loading = ref(false)
const data = ref<Article[]>([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  status: undefined as number | undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const editingId = ref<number | undefined>()
const formRef = ref<FormInstance>()
const form = reactive<ArticleForm>({ title: '', summary: '', content: '', status: 1 })
const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const resp = await pageArticles(query)
    data.value = resp.records
    total.value = resp.total
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  dialogTitle.value = '新建文章'
  editingId.value = undefined
  Object.assign(form, { title: '', summary: '', content: '', status: 1 })
  dialogVisible.value = true
}

const openEdit = (row: Article) => {
  dialogTitle.value = '编辑文章'
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    summary: row.summary ?? '',
    content: row.content ?? '',
    status: row.status
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (editingId.value) {
    await updateArticle(editingId.value, form)
    ElMessage.success('更新成功')
  } else {
    await createArticle(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  await load()
}

const remove = async (row: Article) => {
  await ElMessageBox.confirm(`确定删除《${row.title}》？`, '提示', { type: 'warning' })
  await deleteArticle(row.id)
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
        placeholder="搜索标题"
        clearable
        style="width: 240px"
        @keyup.enter="load"
      />
      <el-select
        v-model="query.status"
        placeholder="状态"
        clearable
        style="width: 120px"
        @change="load"
      >
        <el-option :value="1" label="已发布" />
        <el-option :value="0" label="草稿" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <div class="spacer" />
      <el-button v-if="canWrite()" type="success" @click="openCreate">新建</el-button>
    </div>

    <el-table v-loading="loading" :data="data" border stripe style="margin-top: 12px">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" show-overflow-tooltip />
      <el-table-column prop="summary" label="摘要" show-overflow-tooltip />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column v-if="canWrite()" label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="8" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">已发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
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
