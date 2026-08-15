<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :span="6"><el-card>用户总数<strong> 12</strong></el-card></el-col>
      <el-col :span="6"><el-card>食谱总数<strong> 3</strong></el-card></el-col>
      <el-col :span="6"><el-card>会话总数<strong> 0</strong></el-card></el-col>
      <el-col :span="6"><el-card>采购单<strong> 0</strong></el-card></el-col>
    </el-row>
    <el-card style="margin-top: 16px;">
      <p>欢迎使用无定珍后台。当前为 M1 基础框架里程碑</p>
      <p>后续 M2-M6 将陆续开放。</p>
    </el-card>
  </div>
</template>

<script setup></script>
<style scoped>
.page { padding: 0; }
</style>
