package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@TableName(value = "inspection_media", autoResultMap = true)
public class InspectionMedia {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("data_id")
    private Long dataId;

    @TableField("file_name")
    private String fileName;

    @TableField("storage_path")
    private String storagePath;

    @TableField("mime_type")
    private String mimeType;

    @TableField("file_size")
    private Long fileSize;

    @TableField("thumb_path")
    private String thumbPath;

    @TableField("capture_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime captureTime;

    private Double latitude;
    private Double longitude;
    private Double altitude;

    /**
     * exif 存为 JSON 字段，使用 JacksonTypeHandler 做序列化/反序列化
     */
    @TableField(value = "exif", typeHandler = JacksonTypeHandler.class)
    private String exif;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== GETTERS / SETTERS ==========
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDataId() { return dataId; }
    public void setDataId(Long dataId) { this.dataId = dataId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getThumbPath() { return thumbPath; }
    public void setThumbPath(String thumbPath) { this.thumbPath = thumbPath; }

    public LocalDateTime getCaptureTime() { return captureTime; }
    public void setCaptureTime(LocalDateTime captureTime) { this.captureTime = captureTime; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    public String getExif() { return exif; }
    public void setExif(String exif) { this.exif = exif; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFilePath() {
        return this.storagePath;
    }

    /**
     * 返回文件在磁盘上的绝对路径（用于 ZIP 打包时读取本地文件）
     * 如果 storagePath 是完整 URL（以 http/https 开头）则返回 null（表示非本地文件）
     */
    public String getAbsoluteFilePath(String baseStorageDir) {
        if (storagePath == null) return null;
        String trimmed = storagePath.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return null; // 远程 OSS/URL，不返回本地路径
        }
        // storagePath 可能以 / 开头或没有，拼接时注意
        if (trimmed.startsWith("/")) {
            return baseStorageDir + trimmed;
        } else {
            return baseStorageDir + "/" + trimmed;
        }
    }
}
