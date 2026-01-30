<template>
  <div class="app-container">
    <el-container>
      <el-header class="app-header">
        <h1>
          <el-icon><Link /></el-icon>
          Torrent Utils - 种子工具
        </h1>
      </el-header>
      <el-main class="app-main">
        <el-card class="function-card">
          <el-tabs v-model="activeTab" type="border-card">
            <!-- 种子文件上传 -->
            <el-tab-pane label="种子转磁力链接" name="torrent">
              <template #label>
                <span class="tab-label">
                  <el-icon><Document /></el-icon>
                  种子转磁力链接
                </span>
              </template>
              <TorrentUpload />
            </el-tab-pane>

            <!-- 压缩包上传 -->
            <el-tab-pane label="压缩包处理" name="zip">
              <template #label>
                <span class="tab-label">
                  <el-icon><FolderOpened /></el-icon>
                  压缩包处理
                </span>
              </template>
              <ZipUpload />
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-main>
      <el-footer class="app-footer">
        <span>Torrent Utils &copy; 2024</span>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Link, Document, FolderOpened } from '@element-plus/icons-vue'
import TorrentUpload from './components/TorrentUpload.vue'
import ZipUpload from './components/ZipUpload.vue'

const activeTab = ref('torrent')
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.app-header {
  background-color: rgba(255, 255, 255, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.app-header h1 {
  margin: 0;
  font-size: 28px;
  color: #409eff;
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-main {
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}

.function-card {
  width: 100%;
  max-width: 900px;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
}

.app-footer {
  background-color: rgba(255, 255, 255, 0.9);
  text-align: center;
  color: #606266;
}
</style>
