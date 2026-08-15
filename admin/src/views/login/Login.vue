<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="title">无定珍 PC 后台</div>
      <div class="subtitle">M1 基础框架 · 账号密码登录</div>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px" @keyup.enter="onSubmit">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="admin" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="admin123" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onSubmit">登录</el-button>
      </el-form>
      <div class="hint">默认账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value.validate()
  try {
    loading.value = true
    const data = await authApi.adminLogin(form)
    userStore.setLogin(data.token, data)
    ElMessage.success('登录成功')
    router.push(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #06ae56 0%, #0d8e44 100%);
}
.login-card { width: 420px; padding: 24px; }
.title { font-size: 22px; font-weight: 600; text-align: center; margin-bottom: 8px; }
.subtitle { color: #888; text-align: center; margin-bottom: 24px; }
.hint { color: #aaa; text-align: center; margin-top: 16px; font-size: 12px; }
</style>
