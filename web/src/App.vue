<template>
  <div id="app">
    <el-container>
      <el-header style="background-color: #409EFF; color: white; line-height: 60px; text-align: center;">
        <h1>MetaGPT Mall - 电商平台</h1>
      </el-header>
      <el-container>
        <el-aside width="200px" style="background-color: #ecf5ff; min-height: calc(100vh - 60px);">
          <el-menu router :default-active="$route.path">
            <el-menu-item index="/">
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/health">
              <span>健康检查</span>
            </el-menu-item>
            <!-- 后续可在此添加商品、购物车、订单等菜单项 -->
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
// 使用 Composition API 的 setup 语法糖
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
}
</style>
