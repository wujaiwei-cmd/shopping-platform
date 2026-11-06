import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  // 部署基路径：本地开发 / 默认，GitHub Pages 部署时由构建环境传入 /仓库名/
  base: process.env.VITE_BASE || '/',
  plugins: [vue()],
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        entryFileNames: 'assets/index-[hash].js',
        chunkFileNames: 'assets/[name]-chunk-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]'
      }
    },
    reportCompressedSize: true,
    cssMinify: true
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true
      }
    }
  }
})
