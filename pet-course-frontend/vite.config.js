import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vitejs.dev/config/
export default defineConfig({
  // 核心插件：Vue 支持 + 开发者工具
  plugins: [
    vue(),
    vueDevTools(),
  ],
  // 路径别名：@ 指向 src 目录（方便导入）
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // 关键配置：支持 history 路由模式，防止刷新 404
  server: {
    historyApiFallback: true,
    // 可选：指定端口（如果需要）
    port: 5173,
    // 可选：自动打开浏览器
    open: true
  }
})
