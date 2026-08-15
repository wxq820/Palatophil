<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">无定珍</div>
      <el-menu :default-active="route.path" router :collapse="false" background-color="#001529" text-color="#fff" active-text-color="#06ae56">
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/ingredient">
          <el-icon><Bowl /></el-icon>
          <span>食材库审核</span>
        </el-menu-item>
        <el-menu-item index="/recipe">
          <el-icon><Reading /></el-icon>
          <span>食谱审核</span>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><ShoppingCart /></el-icon>
          <span>采购单管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="title">{{ route.meta.title || '无定珍后台' }}</div>
        <div class="user-info">
          <el-dropdown @command="onCommand">
            <span class="user-name">
              {{ userStore.user?.nickname || '管理员' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

function onCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; }
.logo {
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  padding: 18px;
  text-align: center;
  border-bottom: 1px solid #1f2d3d;
}
.header {
  background: #fff;
  border-bottom: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.title { font-size: 18px; font-weight: 600; }
.user-name { display: flex; align-items: center; gap: 4px; cursor: pointer; }
.main { padding: 24px; }
</style>
