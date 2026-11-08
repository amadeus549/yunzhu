const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  publicPath: './',
  productionSourceMap: false,
  devServer: {
    port: 8888,
    proxy: {
      '/admin': { target: 'http://localhost:8080', changeOrigin: true },
      '/resident': { target: 'http://localhost:8080', changeOrigin: true },
      '/ws': { target: 'ws://localhost:8080', ws: true }
    }
  }
})
