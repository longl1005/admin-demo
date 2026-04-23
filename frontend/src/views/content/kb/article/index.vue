<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import MarkdownIt from 'markdown-it'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  createKbArticle,
  deleteKbArticle,
  fetchKbCategoryTree,
  getKbArticle,
  pageKbArticles,
  updateKbArticle,
  type KbArticle,
  type KbArticleForm,
  type KbCategoryTree
} from '@/api/kb'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const canWrite = () => auth.roles.includes('ROLE_ADMIN')

const md = new MarkdownIt({ html: false, linkify: true, breaks: true })

const loading = ref(false)
const data = ref<KbArticle[]>([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  categoryId: undefined as number | undefined,
  status: undefined as number | undefined
})

const categoryTree = ref<KbCategoryTree[]>([])
const categoryOptions = computed(() => {
  const out: { label: string; value: number }[] = []
  const walk = (nodes: KbCategoryTree[], depth: number) => {
    for (const n of nodes) {
      out.push({ label: `${'— '.repeat(depth)}${n.name}`, value: n.id })
      if (n.children?.length) walk(n.children, depth + 1)
    }
  }
  walk(categoryTree.value, 0)
  return out
})
const categoryName = (id: number | undefined) => {
  if (id == null) return ''
  return categoryOptions.value.find((o) => o.value === id)?.label.replace(/^(— )+/, '') ?? ''
}

const dialogVisible = ref(false)
const dialogTitle = ref('')
const editingId = ref<number | undefined>()
const formRef = ref<FormInstance>()
const form = reactive<KbArticleForm>({
  title: '',
  summary: '',
  content: '',
  tags: '',
  categoryId: undefined,
  status: 1
})
const rules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}

const viewVisible = ref(false)
const viewing = ref<KbArticle | null>(null)
const viewingHtml = computed(() => (viewing.value?.content ? md.render(viewing.value.content) : ''))

const load = async () => {
  loading.value = true
  try {
    const resp = await pageKbArticles(query)
    data.value = resp.records
    total.value = resp.total
  } finally {
    loading.value = false
  }
}

const openView = async (row: KbArticle) => {
  const full = await getKbArticle(row.id)
  viewing.value = full
  viewVisible.value = true
  const idx = data.value.findIndex((a) => a.id === row.id)
  if (idx >= 0) data.value[idx] = { ...data.value[idx], views: full.views }
}

const openCreate = () => {
  dialogTitle.value = '新建知识条目'
  editingId.value = undefined
  Object.assign(form, {
    title: '',
    summary: '',
    content: '',
    tags: '',
    categoryId: query.categoryId,
    status: 1
  })
  dialogVisible.value = true
}

const openEdit = (row: KbArticle) => {
  dialogTitle.value = '编辑知识条目'
  editingId.value = row.id
  Object.assign(form, {
    title: row.title,
    summary: row.summary ?? '',
    content: row.content ?? '',
    tags: row.tags ?? '',
    categoryId: row.categoryId,
    status: row.status
  })
  dialogVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  if (editingId.value) {
    await updateKbArticle(editingId.value, form)
    ElMessage.success('更新成功')
  } else {
    await createKbArticle(form)
    ElMessage.success('创建成功')
  }
  dialogVisible.value = false
  await load()
}

const remove = async (row: KbArticle) => {
  await ElMessageBox.confirm(`确定删除《${row.title}》？`, '提示', { type: 'warning' })
  await deleteKbArticle(row.id)
  ElMessage.success('删除成功')
  await load()
}

onMounted(async () => {
  categoryTree.value = await fetchKbCategoryTree()
  await load()
})
</script>

<template>
  <el-card shadow="never">
    <div class="toolbar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题"
        clearable
        style="width: 220px"
        @keyup.enter="load"
      />
      <el-select
        v-model="query.categoryId"
        placeholder="所属分类"
        clearable
        style="width: 180px"
        @change="load"
      >
        <el-option
          v-for="opt in categoryOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
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
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="标题" min-width="180">
        <template #default="{ row }">
          <el-link type="primary" :underline="false" @click="openView(row)">
            {{ row.title }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="分类" width="140">
        <template #default="{ row }">
          {{ categoryName(row.categoryId) || '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="标签" width="160">
        <template #default="{ row }">
          <el-tag v-for="t in (row.tags || '').split(',').filter(Boolean)" :key="t" size="small" style="margin-right: 4px">
            {{ t }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="views" label="阅读" width="80" />
      <el-table-column label="状态" width="90">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" clearable style="width: 100%">
            <el-option
              v-for="opt in categoryOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="多个标签用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="正文">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="12"
            placeholder="支持 Markdown"
          />
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

    <el-dialog
      v-model="viewVisible"
      :title="viewing?.title || '详情'"
      width="780px"
      top="8vh"
    >
      <div v-if="viewing" class="view-meta">
        <el-tag size="small">{{ categoryName(viewing.categoryId) || '未分类' }}</el-tag>
        <el-tag
          v-for="t in (viewing.tags || '').split(',').filter(Boolean)"
          :key="t"
          size="small"
          type="info"
        >
          {{ t }}
        </el-tag>
        <span class="meta-text">作者：{{ viewing.author || '-' }}</span>
        <span class="meta-text">阅读：{{ viewing.views ?? 0 }}</span>
        <span class="meta-text">更新：{{ viewing.updatedAt }}</span>
      </div>
      <div v-if="viewing?.summary" class="view-summary">{{ viewing.summary }}</div>
      <div class="markdown-body" v-html="viewingHtml" />
    </el-dialog>
  </el-card>
</template>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.spacer {
  flex: 1;
}
.view-meta {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 12px;
}
.meta-text {
  color: #909399;
  font-size: 13px;
}
.view-summary {
  color: #606266;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
  margin-bottom: 12px;
}
</style>

<style>
.markdown-body {
  line-height: 1.75;
  color: #303133;
}
.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  margin: 1em 0 0.5em;
  font-weight: 600;
}
.markdown-body h1 {
  font-size: 1.6em;
}
.markdown-body h2 {
  font-size: 1.35em;
}
.markdown-body h3 {
  font-size: 1.15em;
}
.markdown-body p {
  margin: 0.6em 0;
}
.markdown-body ul,
.markdown-body ol {
  padding-left: 1.8em;
}
.markdown-body code {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
  font-size: 0.92em;
}
.markdown-body pre {
  background: #282c34;
  color: #e6e6e6;
  padding: 12px 14px;
  border-radius: 6px;
  overflow-x: auto;
}
.markdown-body pre code {
  background: transparent;
  color: inherit;
  padding: 0;
}
.markdown-body blockquote {
  border-left: 4px solid #dcdfe6;
  color: #606266;
  margin: 0.8em 0;
  padding: 0.2em 1em;
}
</style>
