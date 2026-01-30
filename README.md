# Torrent Utils - 种子工具

一个基于 Spring Boot + Vue3 的种子文件处理工具，支持批量转换种子为磁力链接、压缩包解压分类等功能。

## 功能特性

- 🔗 **种子转磁力链接**：批量上传 .torrent 文件，快速转换为磁力链接
- 📦 **压缩包处理**：上传包含种子的压缩包（.zip/.rar），自动解压、分类并生成磁力链接汇总
- 🗂️ **智能分类**：根据文件名自动分类种子文件（fc2ppv、carib、1pondo 等）
- 📥 **批量下载**：支持按分类下载磁力链接文件，或一键下载所有分类的汇总压缩包
- 🎨 **现代化界面**：Vue3 + Element Plus 构建的友好用户界面

## 技术栈

### 后端
- Java 8
- Spring Boot 2.7.x
- Spring Web MVC
- junrar（RAR 解压）
- Bencode 编解码（自定义实现）

### 前端
- Vue 3.4+
- TypeScript
- Vite 5
- Element Plus 2.5
- Axios

## 快速开始

### 环境要求

- JDK 8+
- Node.js 16+
- Maven 3.x

### 后端启动

```bash
# 克隆项目
git clone https://github.com/zhangbaio/torrent-utils.git
cd torrent-utils

# 编译并运行
mvn clean package
mvn spring-boot:run

# 或直接运行 JAR
java -jar target/torrentUtils-0.0.1-SNAPSHOT.jar
```

后端服务将在 `http://localhost:9090` 启动

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build
```

前端开发服务器将在 `http://localhost:5173` 启动

## 项目结构

```
torrentUtils/
├── src/main/java/com/example/torrentutils/
│   ├── config/           # 配置类
│   ├── controller/       # REST API 控制器
│   ├── service/          # 业务逻辑层
│   ├── model/            # 数据模型
│   ├── codec/            # Bencode 编解码器
│   ├── exception/        # 异常处理
│   └── torrent/          # 工具类
│       └── torrentUtil/  # 种子文件处理工具
├── frontend/             # Vue3 前端项目
│   ├── src/
│   │   ├── api/         # API 调用
│   │   ├── components/  # Vue 组件
│   │   ├── App.vue      # 主组件
│   │   └── main.ts      # 入口文件
│   ├── index.html
│   ├── package.json
│   └── vite.config.ts
└── README.md
```

## API 接口

### 上传种子文件
```
POST /api/torrent/uploadMultiTorrent
Content-Type: multipart/form-data

参数: fileList (File[]) 种子文件数组
返回: 磁力链接列表
```

### 上传压缩包
```
POST /api/uploadMultiZip
Content-Type: multipart/form-data

参数: fileList (File[]) 压缩包数组
返回: 处理结果和分类信息
```

### 下载文件
```
GET /api/download?filePath=xxx

返回: 文件流
```

### 批量下载
```
POST /api/download/batch
Content-Type: application/json

参数: ["path1", "path2", ...]
返回: ZIP 压缩包
```

## 配置说明

配置文件：`src/main/resources/application.yml`

```yaml
server:
  port: 9090

spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 20MB
      max-request-size: 100MB

app:
  uploadDir: uploads/
  uploadTorrentDir: uploads/torrent/
  uploadZipDir: uploads/zip/
  originalFileDir: 压缩种子文件/
  allFileDir: 磁力链接汇总/
  classifyFileDir: 种子分类/
```

## 文件分类规则

项目内置了以下种子分类规则：

| 分类 | 匹配规则 |
|------|----------|
| fc2ppv | fc2ppv\|fc2-ppv |
| carib | 加勒比\|carib |
| 10-mu | 天然素人\|10-mu\|10musume |
| 1pondo | 一本道\|1pon |
| heyzo | heyzo |
| paco | paco |
| kin8 | kin8 |
| c0390 | c0930 |
| h0930 | h0930 |
| h4610 | h4610 |
| AVF-PPV | AVF-PPV-HEY |

可通过 API 动态添加自定义分类规则。

## 使用示例

### 种子转磁力链接

1. 打开前端页面 `http://localhost:5173`
2. 选择"种子转磁力链接"标签
3. 拖拽或点击选择多个 .torrent 文件
4. 点击"开始转换"
5. 复制单个或全部磁力链接

### 压缩包处理

1. 选择"压缩包处理"标签
2. 上传包含种子的压缩包（.zip/.rar）
3. 系统自动解压、分类、生成磁力链接
4. 查看分类汇总表格
5. 按分类下载或一键下载所有磁力链接文件

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
