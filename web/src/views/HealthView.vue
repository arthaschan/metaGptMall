<template>
  <div class="health-view">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>Backend Health Check</span>
          <el-button 
            type="primary" 
            :loading="loading" 
            @click="checkHealth"
            icon="Refresh"
          >
            Check
          </el-button>
        </div>
      </template>
      
      <div v-if="error" class="error-message">
        <el-alert
          title="Connection Failed"
          type="error"
          :description="error"
          show-icon
          closable
        />
      </div>
      
      <div v-if="healthData">
        <el-descriptions title="Backend Status" border>
          <el-descriptions-item label="Status">
            <el-tag type="success" v-if="healthData.status === 'UP'">UP</el-tag>
            <el-tag type="danger" v-else>DOWN</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="Timestamp">
            {{ formatDate(healthData.timestamp) }}
          </el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <div v-if="healthData.components">
          <h3>Components</h3>
          <el-table :data="componentData" style="width: 100%">
            <el-table-column prop="name" label="Component" />
            <el-table-column prop="status" label="Status">
              <template #default="scope">
                <el-tag :type="scope.row.status === 'UP' ? 'success' : 'danger'">
                  {{ scope.row.status }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <div v-else>
          <p>Raw response data:</p>
          <pre class="raw-data">{{ JSON.stringify(healthData, null, 2) }}</pre>
        </div>
      </div>
      
      <div v-else-if="!error && !loading" class="empty-state">
        <p>Click "Check" to test backend connection</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import http from '../api/http'
import type { AxiosError } from 'axios'

interface HealthData {
  status: string
  timestamp?: string
  components?: Record<string, { status: string }>
}

const loading = ref(false)
const error = ref<string | null>(null)
const healthData = ref<HealthData | null>(null)

const checkHealth = async () => {
  loading.value = true
  error.value = null
  
  try {
    const response = await http.get<HealthData, HealthData>('/api/health')
    healthData.value = response
  } catch (err) {
    const axiosError = err as AxiosError
    if (axiosError.response) {
      // Server responded with error status
      error.value = `Server error: ${axiosError.response.status} ${axiosError.response.statusText}`
    } else if (axiosError.request) {
      // Request made but no response
      error.value = 'No response from server. Make sure backend is running on port 8080.'
    } else {
      // Something else happened
      error.value = `Error: ${axiosError.message}`
    }
    healthData.value = null
  } finally {
    loading.value = false
  }
}

const formatDate = (timestamp?: string) => {
  if (!timestamp) return 'N/A'
  return new Date(timestamp).toLocaleString()
}

const componentData = computed(() => {
  if (!healthData.value?.components) return []
  return Object.entries(healthData.value.components).map(([name, data]) => ({
    name,
    status: data.status
  }))
})
</script>

<style scoped>
.health-view {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.error-message {
  margin-bottom: 20px;
}

.raw-data {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
}
</style>
