package com.huoli.hlai.generate.article;

import com.huoli.hlai.generate.article.service.hotspot.AiModelCoordinator;
import com.huoli.hlai.generate.article.service.hotspot.AiModelService;
import com.huoli.hlai.generate.article.service.hotspot.UniversalHotspotService;
import com.huoli.hlai.generate.article.model.dto.hotspot.RawHotspotItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GenerateArticleApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private UniversalHotspotService universalHotspotService;

    @Autowired
    private List<AiModelService> aiServices;

    @Autowired
    private AiModelCoordinator aiModelCoordinator;

    @Test
    void testBaiduHotspotFetch() {
        // 准备测试关键词
        List<String> keywords = List.of("人工智能", "科技创新", "互联网发展");

        // 调用全网采集服务
        List<RawHotspotItem> result = universalHotspotService.fetchHotspots(keywords, null);

        // 验证结果
        assertNotNull(result, "返回结果不应为 null");

        if (!CollectionUtils.isEmpty(result)) {
            System.out.println("\n===== 抓取成功！共 " + result.size() + " 条数据 =====\n");

            // 打印前 3 条数据
            result.stream()
                    .limit(3)
                    .forEach(item -> {
                        System.out.println("标题：" + item.getTitle());
                        System.out.println("内容：" + item.getContent());
                        System.out.println("来源：" + item.getSource());
                        System.out.println("平台：" + item.getPlatformType());
                        System.out.println("发布时间：" + item.getPublishTime());
                        System.out.println("点赞：" + item.getLikeCount());
                        System.out.println("评论：" + item.getCommentCount());
                        System.out.println("分享：" + item.getShareCount());
                        System.out.println("---");
                    });

            System.out.println("\n===== 测试通过 =====\n");
        } else {
            System.out.println("\n===== 未获取到热点数据，但 API 调用成功 =====\n");
        }
    }

    @Test
    void testAiModelServices() {
        // 测试 AI 模型服务是否都加载成功
        assertNotNull(aiServices, "AI 服务列表不应为 null");
        assertFalse(aiServices.isEmpty(), "AI 服务列表不应为空");

        System.out.println("\n===== 已加载的 AI 模型服务 =====");
        aiServices.forEach(service -> {
            System.out.println("模型类型：" + service.getModelType());
            System.out.println("服务类名：" + service.getClass().getSimpleName());
            System.out.println("---");
        });

        // 测试协调器获取可用模型
        List<String> availableModels = aiModelCoordinator.getAvailableModels();
        assertNotNull(availableModels, "可用模型列表不应为 null");
        assertFalse(availableModels.isEmpty(), "可用模型列表不应为空");

        System.out.println("\n===== 可用的 AI 模型 =====");
        availableModels.forEach(model -> System.out.println("- " + model));
        System.out.println("===========================\n");
    }

}
