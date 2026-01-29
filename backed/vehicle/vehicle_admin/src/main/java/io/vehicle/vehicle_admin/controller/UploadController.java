package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.UploadChunk;
import io.vehicle.vehicle_admin.mapper.UploadChunkMapper;
import jakarta.annotation.Resource;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin
public class UploadController {

    @Value("${app.storage.base:/var/www/myapp}")
    private String baseDir;

    @Resource
    private UploadChunkMapper chunkMapper;

    /**
     * 分片上传接口
     */
    @PostMapping("/chunk")
    public String uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("chunkNumber") int chunkNumber,
            @RequestParam("totalChunks") int totalChunks) throws IOException {

        File chunkDir = new File(baseDir + "/chunks/" + fileMd5);
        if (!chunkDir.exists()) chunkDir.mkdirs();

        File chunkFile = new File(chunkDir, chunkNumber + ".part");
        file.transferTo(chunkFile);

        UploadChunk chunk = new UploadChunk();
        chunk.setFileMd5(fileMd5);
        chunk.setChunkNumber(chunkNumber);
        chunk.setTotalChunks(totalChunks);
        chunk.setChunkPath(chunkFile.getAbsolutePath());
        chunkMapper.insertChunk(chunk);

        return "Chunk " + chunkNumber + " uploaded";
    }

    /**
     * 合并分片接口
     */
    @PostMapping("/merge")
    public String mergeChunks(
            @RequestParam("fileMd5") String fileMd5,
            @RequestParam("fileName") String fileName) throws IOException {

        File chunkDir = new File(baseDir + "/chunks/" + fileMd5);
        if (!chunkDir.exists()) {
            return "No chunks found for file " + fileMd5;
        }

        // 合并后的目标文件路径
        File mergedFile = new File(baseDir + "/uploads/" + fileName);
        File parentDir = mergedFile.getParentFile();
        if (!parentDir.exists()) parentDir.mkdirs();

        // 获取所有分片文件并排序
        List<File> chunkFiles = List.of(chunkDir.listFiles())
                .stream()
                .sorted((a, b) -> {
                    int n1 = Integer.parseInt(a.getName().replace(".part", ""));
                    int n2 = Integer.parseInt(b.getName().replace(".part", ""));
                    return Integer.compare(n1, n2);
                })
                .collect(Collectors.toList());

        // 合并文件
        for (File chunk : chunkFiles) {
            FileUtils.writeByteArrayToFile(
                    mergedFile,
                    FileUtils.readFileToByteArray(chunk),
                    true
            );
        }

        // 删除临时分片文件及数据库记录
        FileUtils.deleteDirectory(chunkDir);
        chunkMapper.deleteByFileMd5(fileMd5);

        return "File merged successfully: " + mergedFile.getAbsolutePath();
    }

}
