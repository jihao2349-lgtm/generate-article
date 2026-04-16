package com.huoli.hlai.generate.article.model.dto.article.template;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章上传DTO
 */
@Schema(name = "ArticleUploadDTO", description = "文章上传参数")
public class ArticleUploadDTO extends UserInfoDTO {
    
    /**
     * 文件列表
     */
    @Schema(description = "文件列表")
    @NotEmpty(message = "文件列表不能为空")
    @Size(max = 10, message = "文件数量不得超过 10 个")
    private List<MultipartFile> fileList;
    
    public List<MultipartFile> getFileList() {
        return fileList;
    }
    
    public void setFileList(List<MultipartFile> fileList) {
        this.fileList = fileList;
    }
}