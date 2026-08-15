<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>食谱管理</span>
        <div class="filters">
          <el-input
            v-model="query.keyword"
            placeholder="食谱名 / 描述"
            clearable
            style="width: 220px"
            @keyup.enter="load(1)"
          />
          <el-select v-model="query.visibility" placeholder="可见性" clearable style="width: 130px" @change="load(1)">
            <el-option label="全部" value="" />
            <el-option label="公开" value="PUBLIC" />
            <el-option label="私有" value="PRIVATE" />
          </el-select>
          <el-select v-model="query.auditStatus" placeholder="审核状态" style="width: 140px" @change="load(1)">
            <el-option label="全部" :value="4" />
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
          <el-button type="primary" @click="load(1)">查询</el-button>
        </div>
      </div>
    </template>

    <el-table :data="rows" v-loading="loading" stripe border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="食谱名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
      <el-table-column prop="version" label="版本" width="70" />
      <el-table-column prop="servings" label="基准份数" width="90" />
      <el-table-column label="可见性" width="90">
        <template #default="{ row }">
          <el-tag :type="VISIBILITY[row.visibility]?.type" size="small">
            {{ VISIBILITY[row.visibility]?.label || row.visibility }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="AUDIT_STATUS[row.auditStatus]?.type" size="small">
            {{ AUDIT_STATUS[row.auditStatus]?.label || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ownerNickname" label="创建者" width="120" />
      <el-table-column prop="ingredientCount" label="食材数" width="80" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="view(row)">查看</el-button>
          <el-button v-if="row.auditStatus === 0" type="success" size="small" @click="audit(row, 1)">通过</el-button>
          <el-button v-if="row.auditStatus === 0" type="danger" size="small" @click="audit(row, 2)">拒绝</el-button>
          <el-button v-if="row.auditStatus !== 1" type="warning" size="small" @click="audit(row, 1)">改通过</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="query.page"
      v-model:page-size="query.size"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      style="margin-top: 16px; justify-content: flex-end"
      @current-change="(p) => load(p)"
      @size-change="(s) => { query.size = s; load(1) }"
    />
  </el-card>

  <el-dialog v-model="detailVisible" title="食谱详情" width="720px" top="6vh">
    <div v-if="current" class="detail">
      <p><strong>名称：</strong>{{ current.name }}</p>
      <p><strong>描述：</strong>{{ current.description || '—' }}</p>
      <p><strong>份数：</strong>{{ current.servings }} | <strong>版本：</strong>{{ current.version }} | <strong>创建者：</strong>{{ current.ownerNickname }}</p>
      <h4>食材块</h4>
      <div v-for="b in current.blocks" :key="b.id" class="block">
        <div class="block-head">【{{ b.name }}】（{{ b.blockType }}）</div>
        <ul>
          <li v-for="i in b.ingredients" :key="i.id">
            {{ i.ingredientName }}<span v-if="i.isAnchor === 1" class="anchor">★</span>
            - {{ i.amountG }} g<span v-if="i.note">（{{ i.note }}）</span>
          </li>
        </ul>
      </div>
      <div v-if="!current.blocks || current.blocks.length === 0" class="muted">暂无食材</div>
    </div>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { recipeApi, VISIBILITY, AUDIT_STATUS } from '@/api/recipe'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 20, keyword: '', visibility: '', auditStatus: 1 })
const detailVisible = ref(false)
const current = ref(null)

async function load(p) {
  if (p) query.page = p
  loading.value = true
  try {
    const data = await recipeApi.page({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      visibility: query.visibility || undefined,
      auditStatus: query.auditStatus
    })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function audit(row, status) {
  try {
    await recipeApi.audit(row.id, status)
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    load()
  } catch (e) { /* ignore */ }
}

async function view(row) {
  try {
    current.value = await recipeApi.detail(row.id)
    detailVisible.value = true
  } catch (e) { /* ignore */ }
}

onMounted(() => load(1))
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.filters { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.muted { color: #c0c4cc; }
.block { margin: 8px 0; padding: 8px 12px; background: #fafafa; border-radius: 6px; }
.block-head { font-weight: 600; margin-bottom: 4px; }
.block ul { margin: 0; padding-left: 16px; }
.anchor { color: #e6a23c; margin-left: 4px; }
</style>
