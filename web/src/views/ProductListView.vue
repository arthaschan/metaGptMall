<template>
  <div>
    <el-page-header content="商品列表" title="MetaGPT Mall" />
    <el-alert
      v-if="error"
      :description="error"
      show-icon
      title="加载失败"
      type="error"
      style="margin-top: 16px;"
    />
    <el-skeleton v-else-if="loading" :rows="6" animated style="margin-top: 16px;" />
    <el-empty v-else-if="products.length === 0" description="暂无可售商品" style="margin-top: 32px;" />
    <el-row v-else :gutter="16" style="margin-top: 16px;">
      <el-col v-for="product in products" :key="product.id" :lg="8" :md="12" :sm="24" :xl="6" :xs="24">
        <el-card shadow="hover" style="margin-bottom: 16px;">
          <template #header>
            <div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
              <span>{{ product.title }}</span>
              <el-tag type="success">{{ formatPrice(product.priceCents, product.currency) }}</el-tag>
            </div>
          </template>
          <p style="min-height: 72px; color: #606266; line-height: 1.6;">{{ product.description }}</p>
          <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px;">
            <span>库存 {{ product.stock }}</span>
            <el-button type="primary" @click="goToDetail(product.id)">查看详情</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import http from '../api/http'

interface ProductResponse {
  id: number
  title: string
  description: string
  priceCents: number
  currency: string
  stock: number
  imageUrl: string
}

const router = useRouter()
const loading = ref(false)
const error = ref('')
const products = ref<ProductResponse[]>([])

const fetchProducts = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await http.get<ProductResponse[], ProductResponse[]>('/products')
    products.value = data
  } catch (err: any) {
    error.value = err.message || '商品列表加载失败'
  } finally {
    loading.value = false
  }
}

const formatPrice = (priceCents: number, currency: string) => {
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency
  }).format(priceCents / 100)
}

const goToDetail = (id: number) => {
  router.push(`/products/${id}`)
}

onMounted(() => {
  fetchProducts()
})
</script>