<template>
  <el-card>
    <template #header>
      <div class="header">
        <span>食材库管理</span>
        <div class="filters">
          <el-input
            v-model="query.keyword"
            placeholder="食材名 / 别名"
            clearable
            style="width: 200px"
            @keyup.enter="load(1)"
          />
          <el-select v-model="query.category" placeholder="分类" clearable style="width: 140px" @change="load(1)">
            <el-option v-for="c in CATEGORIES" :key="c.value" :label="c.label" :value="c.value" />
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
      <el-table-column prop="name" label="食材名" min-width="120" />
      <el-table-column label="别名" min-width="160">
        <template #default="{ row }">
          <span v-if="row.aliases && row.aliases.length">{{ row.aliases.join('、') }}</span>
          <span v-else class="muted">—</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="110">
        <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
      </el-table-column>
      <el-table-column prop="unitDensity" label="密度 kg/L" width="120" />
      <el-table-column label="系统食材" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.isSystem === 1" type="success" size="small">系统</el-tag>
          <el-tag v-else type="info" size="small">用户</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核状态" width="110">
        <template #default="{ row }">
          <el-tag :type="AUDIT_STATUS[row.auditStatus]?.type" size="small">
            {{ AUDIT_STATUS[row.auditStatus]?.label || '-' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ownerId" label="Owner ID" width="90" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.auditStatus === 0" type="success" size="small" @click="audit(row, 1)">通过</el-button>
          <el-button v-if="row.auditStatus === 0" type="danger"  size="small" @click="audit(row, 2)">拒绝</el-button>
          <el-button v-if="row.auditStatus !== 1" type="warning" size="small" @click="audit(row, 1)">改通过</el-button>
          <el-popconfirm title="确认删除？" @confirm="remove(row)">
            <template #reference>
              <el-button type="danger" size="small" plain>删除</el-button>
            </template>
          </el-popconfirm>
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
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ingredientApi, CATEGORIES, AUDIT_STATUS } from '@/api/ingredient'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ page: 1, size: 20, keyword: '', category: '', auditStatus: 1 })

async function load(p) {
  if (p) query.page = p
  loading.value = true
  try {
    const data = await ingredientApi.page({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      category: query.category || undefined,
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
    await ingredientApi.audit(row.id, status)
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    load()
  } catch (e) { /* axios 拦截器已提示 */ }
}

async function remove(row) {
  try {
    await ingredientApi.remove(row.id)
    ElMessage.success('已删除')
    load()
  } catch (e) { /* ignore */ }
}

function categoryLabel(code) {
  return CATEGORIES.find((c) => c.value === code)?.label || code
}

onMounted(() => load(1))
</script>

<style scoped>
.header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.filters { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.muted { color: #c0c4cc; }
</style>
