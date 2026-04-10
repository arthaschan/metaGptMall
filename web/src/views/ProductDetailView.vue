<template>
  <div>
    <el-page-header @back="router.push('/products')" content="商品详情" title="MetaGPT Mall" />
    <el-alert
      v-if="error"
      :description="error"
      show-icon
      title="加载失败"
      type="error"
      style="margin-top: 16px;"
    />
    <el-skeleton v-else-if="loading" :rows="8" animated style="margin-top: 16px;" />
    <el-card v-else-if="product" shadow="never" style="margin-top: 16px;">
      <div style="display: grid; gap: 16px;">
        <div style="display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap;">
          <div>
            <h2 style="margin: 0 0 8px;">{{ product.title }}</h2>
            <div style="color: #909399;">库存 {{ product.stock }}</div>
          </div>
          <el-tag effect="dark" size="large" type="success">{{ formatPrice(product.priceCents, product.currency) }}</el-tag>
        </div>
        <p style="line-height: 1.8; color: #606266; margin: 0;">{{ product.description }}</p>
        <el-link v-if="product.imageUrl" :href="product.imageUrl" target="_blank" type="primary">查看商品图片</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
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

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const product = ref<ProductResponse | null>(null)

const fetchProduct = async () => {
  loading.value = true
  error.value = ''
  try {
    const data = await http.get<ProductResponse, ProductResponse>(`/products/${route.params.id}`)
    product.value = data
  } catch (err: any) {
    error.value = err.message || '商品详情加载失败'
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

onMounted(() => {
  fetchProduct()
})
</script>