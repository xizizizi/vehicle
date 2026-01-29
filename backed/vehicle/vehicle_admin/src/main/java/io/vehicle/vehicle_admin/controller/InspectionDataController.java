package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.InspectionData;
import io.vehicle.vehicle_admin.entity.InspectionMedia;
import io.vehicle.vehicle_admin.mapper.InspectionDataMapper;
import io.vehicle.vehicle_admin.mapper.InspectionMediaMapper;
import io.vehicle.vehicle_admin.service.InspectionDataService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/reports/data")
@CrossOrigin
public class InspectionDataController {

    @Resource
    private InspectionDataService inspectionDataService;

    // ✅ 读取 application.yml 中配置的路径
    @Value("${app.storage.base:E:/java/java-test1/vehicle/\\storage/uploads}")
    private String storageBase;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadData(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) String droneId,
            @RequestParam(required = false) Integer uploaderId,
            @RequestParam(required = false) String description,
            @RequestParam("files") MultipartFile[] files
    ) {
        try {
            System.out.println("📂 开始上传: " + Arrays.toString(Arrays.stream(files).map(MultipartFile::getOriginalFilename).toArray()));
            System.out.println("📁 存储根路径: " + storageBase);

            InspectionData data = new InspectionData();
            data.setTaskName(taskName);
            data.setAreaId(areaId);
            data.setDroneId(droneId);
            data.setUploaderId(uploaderId);
            data.setDescription(description);
            data.setStatus(InspectionData.Status.PENDING);
            data.setCreatedAt(java.time.LocalDateTime.now());

            InspectionData saved = inspectionDataService.createDataWithFiles(data, files, storageBase);
            Map<String, Object> res = new HashMap<>();
            res.put("id", saved.getId());
            res.put("fileCount", saved.getFileCount());
            return ResponseEntity.ok(res);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", ex.getClass().getSimpleName() + ": " + ex.getMessage()
            ));
        }
    }



    // 列表查询（分页参数 offset/limit）
    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String droneId,
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) Double minLat,
            @RequestParam(required = false) Double maxLat,
            @RequestParam(required = false) Double minLng,
            @RequestParam(required = false) Double maxLng,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        Map<String,Object> params = new HashMap<>();
        params.put("taskName", taskName);
        params.put("droneId", droneId);
        params.put("areaId", areaId);
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        params.put("minLat", minLat);
        params.put("maxLat", maxLat);
        params.put("minLng", minLng);
        params.put("maxLng", maxLng);
        params.put("offset", offset);
        params.put("limit", limit);

        List<InspectionData> list = inspectionDataService.queryList(params);
        return ResponseEntity.ok(list);
    }

    // 获取详情 + 媒体
    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@PathVariable("id") Long id) {
        InspectionData data = inspectionDataService.getById(id);
        if (data == null) return ResponseEntity.notFound().build();
        List<InspectionMedia> media = inspectionDataService.getMediaByDataId(id);
        Map<String,Object> res = new HashMap<>();
        res.put("data", data);
        res.put("media", media);
        return ResponseEntity.ok(res);
    }
//    @GetMapping("/data/{id}/download-zip")
//    public void downloadZip(@PathVariable Long id, HttpServletResponse response) throws IOException {
//        InspectionMediaMapper mediaMapper = null;
//        List<InspectionMedia> medias = mediaMapper.findByDataId(id);
//        if (medias.isEmpty()) {
//            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
//            return;
//        }
//
//        // 设置响应头
//        response.setContentType("application/zip");
//        response.setHeader("Content-Disposition", "attachment; filename=inspection_" + id + ".zip");
//
//        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
//            for (InspectionMedia media : medias) {
//                Path path = Paths.get(media.getFilePath());
//                if (!Files.exists(path)) continue;
//
//                zipOut.putNextEntry(new ZipEntry(path.getFileName().toString()));
//                Files.copy(path, zipOut);
//                zipOut.closeEntry();
//            }
//        }
//

    @GetMapping("/download-zip/{id}")
    public void downloadZip(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            List<InspectionMedia> medias = inspectionDataService.getMediaByDataId(id);
            if (medias == null || medias.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"no media found\"}");
                return;
            }

            String zipFileName = "inspection_" + id + ".zip";
            String encodedName = URLEncoder.encode(zipFileName, "UTF-8").replace("+", "%20");

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", STR."attachment; filename*=UTF-8''\{encodedName}");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                byte[] buffer = new byte[8192];
                for (InspectionMedia media : medias) {
                    String absPath = storageBase + media.getStoragePath();
                    Path filePath = Paths.get(absPath);
                    if (!Files.exists(filePath)) continue;

                    zos.putNextEntry(new ZipEntry(media.getFileName()));
                    try (InputStream fis = Files.newInputStream(filePath)) {
                        int len;
                        while ((len = fis.read(buffer)) != -1) {
                            zos.write(buffer, 0, len);
                        }
                    }
                    zos.closeEntry();
                }
                zos.finish();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(STR."{\"error\":\"\{ex.getMessage()}\"}");
            } catch (IOException ignored) {}
        }
    }

}

