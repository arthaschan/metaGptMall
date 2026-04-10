<template>
  <div>
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>后端健康检查</span>
          <el-button type="primary" @click="fetchHealth">点击检查</el-button>
        </div>
      </template>
      <div v-if="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在检查后端服务...
      </div>
      <div v-else-if="error">
        <el-alert title="请求失败" type="error" :description="error" show-icon />
      </div>
      <div v-else-if="healthData">
        <el-alert title="后端服务正常" type="success" show-icon />
        <el-divider />
        <h4>返回数据：</h4>
        <pre style="background-color: #f5f7fa; padding: 16px; border-radius: 4px; overflow: auto;">{{ formattedData }}</pre>
      </div>
      <div v-else>
        <p>点击上方按钮检查后端服务状态。</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import http from '../api/http'

interface HealthResponse {
  // 根据实际后端接口定义调整
  status?: string
  timestamp?: string
  [key: string]: any
}

const loading = ref(false)
const error = ref('')
const healthData = ref<HealthResponse | null>(null)

const fetchHealth = async () => {
  loading.value = true
  error.value = ''
  healthData.value = null
  try {
    // 请求后端 GET /api/health 端点
    const data = await http.get<HealthResponse>('/health')
    healthData.value = data
  } catch (err: any) {
    error.value = err.message || '未知错误'
    console.error('获取健康状态失败:', err)
  } finally {
    loading.value = false
  }
}

const formattedData = computed(() => {
  return JSON.stringify(healthData.value, null, 2)
})
</script>
