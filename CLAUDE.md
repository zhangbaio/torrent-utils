# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application that provides torrent and compressed file processing utilities. The main functionality includes:
- Extracting ZIP/RAR archives containing torrent files
- Converting torrent files to magnet links
- Classifying files into categories based on regex patterns
- Generating magnet link summary files

## Build and Run Commands

```bash
# Build the project
mvn clean package

# Run the application (Java 8 required)
mvn spring-boot:run

# Or run the JAR directly after building
java -jar target/torrentUtils-0.0.1-SNAPSHOT.jar

# Run tests
mvn test
```

## Architecture

### Package Structure

```
com.example.torrentutils/
├── TorrentUtilsApplication.java    # Main Spring Boot entry point
└── torrent/
    ├── controller/                  # REST API layer
    │   └── PathVariableController  # Main controller with all endpoints
    ├── service/                     # Business logic layer
    │   ├── FileService             # File processing (zip/torrent handling)
    │   ├── TorrentService          # Torrent file operations
    │   └── FolderService           # Folder extraction operations
    └── torrentUtil/                # Utility classes
        ├── TorrentToMagnetConverter  # Bencode codec + magnet link generation
        ├── RegexFileUtil            # File classification by regex patterns
        ├── FileNameModifier         # Filename cleaning
        ├── FileProcessorMac         # Mac-specific file operations
        └── UnarExtractor            # Alternative extraction using unar
```

### Key Design Patterns

- **Custom Bencode Codec**: `TorrentToMagnetConverter` contains a full bencode decoder/encoder implementation for parsing torrent files and calculating info hashes (required for magnet link generation)
- **Regex-Based Classification**: `RegexFileUtil` uses a static map of compiled patterns to classify files into categories (fc2ppv, carib, 1pondo, etc.)
- **File Processing Pipeline**: Upload → Extract → Classify → Generate magnet links

## Configuration

All paths are configurable via `application.yml`:

```yaml
server:
  port: 9090

app:
  uploadDir: uploads/
  uploadTorrentDir: uploads/torrent/
  uploadZipDir: uploads/zip/
  originalFileDir: 压缩种子文件/
  allFileDir: 磁力链接汇总/
  classifyFileDir: 种子分类/
```

## API Endpoints

Main endpoints in `PathVariableController`:

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/uploadMultiZip` | Upload multiple ZIP/RAR files, extract contents, classify, generate magnet links |
| POST | `/torrent/uploadMultiTorrent` | Upload torrent files and convert to magnet links |
| POST | `/upload` | Single file upload |
| POST | `/unzipFile` | Extract a specific file |
| POST | `/unZipFolder` | Extract all files in a folder |

## Important Notes

- **Java 8**: Project targets Java 8 (source/target 1.8)
- **File Upload Limits**: Max 20MB per file, 100MB total request
- **Chinese Characters**: Directory names contain Chinese characters - ensure proper encoding handling
- **Bencode Implementation**: The `TorrentToMagnetConverter` class has custom bencode encoder/decoder - use it for any torrent file parsing needs
- **File Classification**: Patterns are defined in `RegexFileUtil.patternStringMap` static block
- **Copy vs Move**: `RegexFileUtil.classifySingleFile` copies files (not moves) - change `Files.copy` to `Files.move` if move behavior is desired
