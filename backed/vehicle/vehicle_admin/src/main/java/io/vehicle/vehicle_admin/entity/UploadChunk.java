package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName(value = "upload_chunks")
public class UploadChunk {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 为了匹配 Controller 中的 fileMd5 参数，这里需要映射为 upload_id 字段
    @TableField(value = "upload_id")
    private String fileMd5;

    // 为了匹配 Controller 中的 chunkNumber 参数，这里需要映射为 chunk_index 字段
    @TableField(value = "chunk_index")
    private Integer chunkNumber;

    // 为了匹配 Controller 中的 totalChunks 参数，这里需要添加 total_chunks 字段
    @TableField(value = "total_chunks")
    private Integer totalChunks;

    // 添加 chunk_path 字段来存储分块文件路径
    @TableField(value = "chunk_path")
    private String chunkPath;

    // 数据库中已有的字段，但在 Controller 中未使用，可以保留
    @TableField(value = "chunk_size")
    private Integer chunkSize;

    @TableField(value = "total_size")
    private Long totalSize;

    @TableField(value = "file_name")
    private String fileName;

    @TableField(value = "uploaded_at")
    private LocalDateTime uploadedAt;

    // 默认构造函数
    public UploadChunk() {
    }

    // 简化构造函数，用于 Controller
    public UploadChunk(String fileMd5, Integer chunkNumber, Integer totalChunks, String chunkPath) {
        this.fileMd5 = fileMd5;
        this.chunkNumber = chunkNumber;
        this.totalChunks = totalChunks;
        this.chunkPath = chunkPath;
        this.uploadedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "UploadChunk{" +
                "id=" + id +
                ", fileMd5='" + fileMd5 + '\'' +
                ", chunkNumber=" + chunkNumber +
                ", totalChunks=" + totalChunks +
                ", chunkPath='" + chunkPath + '\'' +
                ", chunkSize=" + chunkSize +
                ", totalSize=" + totalSize +
                ", fileName='" + fileName + '\'' +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}