<template>
  <div class="torrent-upload">
    <el-alert
      title="功能说明"
      type="info"
      :closable="false"
      show-icon
      class="mb-4"
    >
      <template #default>
        <p>上传多个种子文件（.torrent），批量转换为磁力链接。支持复制所有磁力链接或单个复制。</p>
      </template>
    </el-alert>

    <el-upload
      ref="uploadRef"
      class="upload-area"
      drag
      :action="uploadAction"
      :auto-upload="false"
      :on-change="handleFileChange"
      :on-remove="handleFileRemove"
      v-model:file-list="fileList"
      :limit="50"
      multiple
      accept=".torrent"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将种子文件拖到此处，或<em>点击选择</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 .torrent 格式，单文件最大 20MB，最多可选择 50 个文件
        </div>
      </template>
    </el-upload>

    <div class="action-buttons" v-if="fileList.length > 0">
      <el-button
        type="primary"
        size="large"
        :loading="loading"
        @click="handleUpload"
      >
        <el-icon><Upload /></el-icon>
        开始转换
      </el-button>
      <el-button size="large" @click="handleClear">
        <el-icon><Delete /></el-icon>
        清空列表
      </el-button>
    </div>

    <!-- 磁力链接结果 -->
    <div v-if="magnetLinks.length > 0" class="result-area">
      <el-divider content-position="left">
        <el-icon><Link /></el-icon>
        转换结果 ({{ magnetLinks.length }} 条)
      </el-divider>

      <div class="result-header">
        <el-button
          type="success"
          size="small"
          @click="copyAllMagnets"
        >
          <el-icon><DocumentCopy /></el-icon>
          复制全部磁力链接
        </el-button>
      </div>

      <div class="magnet-list">
        <div
          v-for="(magnet, index) in magnetLinks"
          :key="index"
          class="magnet-item"
        >
          <div class="magnet-index">{{ index + 1 }}</div>
          <el-input
            :model-value="magnet"
            readonly
            size="small"
          >
            <template #append>
              <el-button @click="copyMagnet(magnet)">
                <el-icon><DocumentCopy /></el-icon>
                复制
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Upload, Delete, Link, DocumentCopy } from '@element-plus/icons-vue'
import type { UploadFile, UploadUserFile } from 'element-plus'
import { uploadMultiTorrent } from '@/api/torrent'

const uploadAction = '/api/torrent/uploadMultiTorrent'
const uploadRef = ref()
const loading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const magnetLinks = ref<string[]>([])

const handleFileChange = (file: UploadFile) => {
  if (!file.name.endsWith('.torrent')) {
    ElMessage.error('只能上传 .torrent 格式的文件')
    return false
  }
}

const handleFileRemove = () => {
  magnetLinks.value = []
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  loading.value = true
  magnetLinks.value = []

  try {
    const files = fileList.value
      .map((item) => item.raw)
      .filter((f): f is File => f !== undefined)

    const response = await uploadMultiTorrent(files)
    const result = response.data.data

    if (result?.magnetLinks) {
      magnetLinks.value = result.magnetLinks
      ElMessage.success(`成功转换 ${magnetLinks.value.length} 个种子文件`)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleClear = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
  magnetLinks.value = []
}

const copyMagnet = async (magnet: string) => {
  try {
    await navigator.clipboard.writeText(magnet)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

const copyAllMagnets = async () => {
  try {
    const allMagnets = magnetLinks.value.join('\n')
    await navigator.clipboard.writeText(allMagnets)
    ElMessage.success(`已复制 ${magnetLinks.value.length} 条磁力链接`)
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.torrent-upload {
  padding: 20px;
}

.mb-4 {
  margin-bottom: 16px;
}

.upload-area {
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.result-area {
  margin-top: 20px;
}

.result-header {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

.magnet-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.magnet-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.magnet-index {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  font-size: 14px;
  font-weight: bold;
  flex-shrink: 0;
}

:deep(.el-input-group__append) {
  padding: 0;
}

:deep(.el-input-group__append .el-button) {
  border: none;
  border-radius: 0;
}
</style>
