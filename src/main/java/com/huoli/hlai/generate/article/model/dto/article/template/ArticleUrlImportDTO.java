package com.huoli.hlai.generate.article.model.dto.article.template;

import com.huoli.hlai.generate.article.model.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 文章导入DTO
 */
@Schema(name = "ArticleUrlImportDTO", description = "文章链接导入参数")
public class ArticleUrlImportDTO extends UserInfoDTO
{
    
    /**
     * URL列表
     */
    @Schema(description = "URL列表", example = "[\"https://example.com/article1\", \"https://example.com/article2\"]")
    @NotEmpty(message = "链接不能为空")
    @Size(max = 10, message = "链接数量不得超过 10 个")
    private List<String> urlList;
    
    public List<String> getUrlList() {
        return urlList;
    }
    
    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }
}