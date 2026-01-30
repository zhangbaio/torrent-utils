<template>
  <div class="zip-upload">
    <el-alert
      title="功能说明"
      type="info"
      :closable="false"
      show-icon
      class="mb-4"
    >
      <template #default>
        <p>上传包含种子文件的压缩包（.zip/.rar），系统会自动解压、分类并生成磁力链接汇总文件供下载。</p>
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
      :limit="20"
      multiple
      accept=".zip,.rar,.7z"
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        将压缩包拖到此处，或<em>点击选择</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          支持 .zip、.rar、.7z 格式，单文件最大 20MB，最多可选择 20 个文件
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
        开始处理
      </el-button>
      <el-button size="large" @click="handleClear">
        <el-icon><Delete /></el-icon>
        清空列表
      </el-button>
    </div>

    <!-- 处理进度 -->
    <div v-if="loading" class="progress-area">
      <el-progress
        :percentage="progress"
        :status="progressStatus"
        :indeterminate="true"
      >
        <span>{{ progressText }}</span>
      </el-progress>
    </div>

    <!-- 处理结果 -->
    <div v-if="result" class="result-area">
      <el-divider content-position="left">
        <el-icon><CircleCheck /></el-icon>
        处理完成
      </el-divider>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="处理文件数">
          {{ result.fileCount }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="success">{{ result.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 分类信息 -->
      <div v-if="result.categories && result.categories.length > 0" class="categories">
        <h4>分类汇总</h4>
        <el-table :data="result.categories" border>
          <el-table-column prop="categoryName" label="分类名称" width="180" />
          <el-table-column prop="fileCount" label="文件数量" width="120" />
          <el-table-column label="操作" align="center">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                @click="downloadMagnetFile(row)"
              >
                <el-icon><Download /></el-icon>
                下载磁力链接
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 全部下载按钮 -->
      <div class="download-all" v-if="result.categories && result.categories.length > 0">
        <el-button
          type="success"
          size="large"
          @click="downloadAllMagnets"
        >
          <el-icon><Download /></el-icon>
          下载所有分类的磁力链接文件
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  UploadFilled,
  Upload,
  Delete,
  CircleCheck,
  Download
} from '@element-plus/icons-vue'
import type { UploadFile, UploadUserFile } from 'element-plus'
import { uploadMultiZip, downloadFile, downloadBatch } from '@/api/torrent'
import type { WorkflowResult, CategoryInfo } from '@/api/types'

const uploadAction = '/api/uploadMultiZip'
const uploadRef = ref()
const loading = ref(false)
const fileList = ref<UploadUserFile[]>([])
const result = ref<WorkflowResult | null>(null)
const progress = ref(0)
const progressText = ref('正在处理中...')
const progressStatus = ref<'success' | 'exception' | 'warning' | ''>('')

const handleFileChange = (file: UploadFile) => {
  const name = file.name.toLowerCase()
  const validExtensions = ['.zip', '.rar', '.7z']
  const isValid = validExtensions.some((ext) => name.endsWith(ext))

  if (!isValid) {
    ElMessage.error('只能上传 .zip、.rar、.7z 格式的压缩包')
    return false
  }
}

const handleFileRemove = () => {
  result.value = null
}

const handleUpload = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请先选择文件')
    return
  }

  loading.value = true
  result.value = null
  progress.value = 0
  progressText.value = '正在上传文件...'
  progressStatus.value = ''

  try {
    const files = fileList.value
      .map((item) => item.raw)
      .filter((f): f is File => f !== undefined)

    const response = await uploadMultiZip(files)
    result.value = response.data.data

    progress.value = 100
    progressText.value = '处理完成'
    progressStatus.value = 'success'

    ElMessage.success('文件处理完成')
  } catch (error: any) {
    progress.value = 100
    progressText.value = '处理失败'
    progressStatus.value = 'exception'
    ElMessage.error(error.message || '处理失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleClear = () => {
  uploadRef.value?.clearFiles()
  fileList.value = []
  result.value = null
  progress.value = 0
}

const downloadMagnetFile = async (category: CategoryInfo) => {
  try {
    ElMessage.info('正在下载...')
    const response = await downloadFile(category.magnetFile)

    // 从响应头获取文件名，优先使用 filename*
    const contentDisposition = response.headers['content-disposition']
    let filename = category.categoryName + '.txt'

    if (contentDisposition) {
      // 先尝试匹配 filename* (RFC 5987)
      const starMatch = contentDisposition.match(/filename\*=UTF-8''([^;\s]+)/i)
      if (starMatch) {
        filename = decodeURIComponent(starMatch[1])
      } else {
        // 尝试匹配普通 filename
        const normalMatch = contentDisposition.match(/filename="([^"]+)"/i)
        if (normalMatch) {
          filename = normalMatch[1]
        }
      }
    }

    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载完成')
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}

const downloadAllMagnets = async () => {
  if (!result.value?.categories) return

  try {
    ElMessage.info('正在打包下载...')
    const filePaths = result.value.categories.map((c) => c.magnetFile)
    const response = await downloadBatch(filePaths)

    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '磁力链接汇总.zip'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('下载完成')
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}
</script>

<style scoped>
.zip-upload {
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

.progress-area {
  margin: 20px 0;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.result-area {
  margin-top: 20px;
}

.categories {
  margin-top: 20px;
}

.categories h4 {
  margin-bottom: 12px;
  color: #303133;
}

.download-all {
  margin-top: 20px;
  text-align: center;
}
</style>
