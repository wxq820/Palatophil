<template>
  <el-card>
    <template #header>用户管理</template>
    <el-table :data="rows" stripe>
      <el-table-column prop="id"       label="ID"     width="80" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="openid"   label="OpenID" />
      <el-table-column prop="role"     label="角色"   width="120" />
      <el-table-column prop="status"   label="状态"   width="100" />
    </el-table>
  </el-card>
</template>

<script setup>
const rows = [
  { id: 1, nickname: '系统管理员', openid: '-', role: 'ADMIN', status: '正常' },
  { id: 2, nickname: '小明',       openid: '-', role: 'USER',  status: '正常' }
]
</script>
