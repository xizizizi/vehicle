package io.vehicle.vehicle_admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.*;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import io.vehicle.vehicle_admin.entity.InspectionData;
import io.vehicle.vehicle_admin.entity.InspectionMedia;
import io.vehicle.vehicle_admin.mapper.InspectionDataMapper;
import jakarta.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InspectionDataService {

    @Resource
    private InspectionDataMapper dataMapper;

    // ✅ 上传文件及元数据
    public InspectionData createDataWithFiles(InspectionData data, MultipartFile[] files, String storageBasePath) throws Exception {
        data.setCreatedAt(LocalDateTime.now());
        dataMapper.insertData(data); // id 会回填

        int count = 0;
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;

            InspectionMedia media = new InspectionMedia();
            media.setDataId(data.getId());
            String original = file.getOriginalFilename();
            media.setFileName(original);

            // ✅ 生成路径
            String ext = FilenameUtils.getExtension(original);
            String newName = UUID.randomUUID().toString() + (ext != null && !ext.isEmpty() ? "." + ext : "");
            String relativePath = "/uploads/" + LocalDateTime.now().toLocalDate() + "/" + newName;
            String fullPath = storageBasePath + relativePath;

            // ✅ 确保目录存在并可写
            java.io.File diskFile = new java.io.File(fullPath);
            diskFile.getParentFile().mkdirs();
            if (!diskFile.getParentFile().canWrite()) {
                throw new RuntimeException("无法写入文件目录：" + diskFile.getParent());
            }

            // ✅ 保存文件
            file.transferTo(diskFile);
            media.setStoragePath(relativePath);
            media.setMimeType(file.getContentType());
            media.setFileSize(file.getSize());

            // ✅ 提取EXIF
            try (InputStream is = file.getInputStream()) {
                Metadata metadata = ImageMetadataReader.readMetadata(is);

                GpsDirectory gps = metadata.getFirstDirectoryOfType(GpsDirectory.class);
                if (gps != null && gps.getGeoLocation() != null) {
                    media.setLatitude(gps.getGeoLocation().getLatitude());
                    media.setLongitude(gps.getGeoLocation().getLongitude());
                }

                Directory exifDir = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                if (exifDir != null) {
                    Date dt = exifDir.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    if (dt != null)
                        media.setCaptureTime(LocalDateTime.ofInstant(dt.toInstant(), java.time.ZoneId.systemDefault()));
                }

                Map<String, Object> exifMap = new HashMap<>();
                for (Directory d : metadata.getDirectories()) {
                    for (Tag t : d.getTags()) {
                        exifMap.put(d.getName() + ":" + t.getTagName(), t.getDescription());
                    }
                }
                media.setExif(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(exifMap));
            } catch (Exception ex) {
                // 图片或视频无法读取 EXIF 的情况忽略
            }

            dataMapper.insertMedia(media);
            count++;
        }

        // ✅ 更新 file_count（手动实现 update）
        data.setFileCount(count);
        dataMapper.updateFileCount(data.getId(), count);

        return data;
    }

    public InspectionData getById(Long id) {
        return dataMapper.selectById(id);
    }

    public List<InspectionData> queryList(Map<String, Object> params) {
        return dataMapper.queryList(params);
    }

    public List<InspectionMedia> getMediaByDataId(Long dataId) {
        return dataMapper.selectMediaByDataId(dataId);
    }

    // 在 InspectionDataService 中添加方法
    public List<InspectionData> getAllInspectionData() {
        // 如果有复杂的查询逻辑，可以在这里实现
        // 否则可以直接调用mapper
        return dataMapper.selectList(null);
    }

    public List<InspectionData> getCompletedInspectionData() {
        // 查询已完成的巡检数据
        // 具体实现根据你的查询条件来定
        QueryWrapper<InspectionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "COMPLETED"); // 根据实际状态字段名调整
        return dataMapper.selectList(queryWrapper);
    }

    // 在 InspectionDataService 中添加方法
    public List<InspectionData> getAllCompletedInspectionData() {
        // 方法1：使用QueryWrapper（MyBatis Plus）
        QueryWrapper<InspectionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "COMPLETED"); // 根据实际数据库字段名
        return dataMapper.selectList(queryWrapper);
    }

    public long countCompletedInspectionData() {
        QueryWrapper<InspectionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "COMPLETED");
        return dataMapper.selectCount(queryWrapper);
    }

    public long countInspectionDataByDateRange(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<InspectionData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "COMPLETED")
                .ge("updated_at", start)  // 或者 created_at，根据业务逻辑
                .le("updated_at", end);
        return dataMapper.selectCount(queryWrapper);
    }
}
