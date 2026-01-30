import request from './index'
import type { ApiResponse, WorkflowResult } from './types'

/**
 * 上传多个种子文件
 */
export function uploadMultiTorrent(files: File[]) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('fileList', file)
  })

  return request.post<ApiResponse<WorkflowResult>>('/torrent/uploadMultiTorrent', formData)
}

/**
 * 上传多个压缩包文件
 */
export function uploadMultiZip(files: File[]) {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('fileList', file)
  })

  return request.post<ApiResponse<WorkflowResult>>('/uploadMultiZip', formData)
}

/**
 * 下载文件
 */
export function downloadFile(filePath: string) {
  return request.get('/download', {
    params: { filePath },
    responseType: 'blob'
  })
}

/**
 * 批量下载文件
 */
export function downloadBatch(filePaths: string[]) {
  return request.post('/download/batch', filePaths, {
    headers: {
      'Content-Type': 'application/json'
    },
    responseType: 'blob'
  })
}

