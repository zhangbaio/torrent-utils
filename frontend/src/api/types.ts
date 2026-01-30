/**
 * API 响应类型
 */
export interface ApiResponse<T = any> {
  code: string
  message: string
  data: T | null
}

/**
 * 工作流结果
 */
export interface WorkflowResult {
  fileCount: number
  fileDir: string
  magnetLinks?: string[]
  categories?: CategoryInfo[]
  status: string
}

/**
 * 分类信息
 */
export interface CategoryInfo {
  categoryName: string
  fileCount: number
  magnetFile: string
}
