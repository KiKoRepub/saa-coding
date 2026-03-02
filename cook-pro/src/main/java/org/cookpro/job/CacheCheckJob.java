package org.cookpro.job;

import jakarta.annotation.Resource;
import org.cookpro.service.MemoryCacheService;
import org.cookpro.service.UserPreferenceService;
import org.cookpro.utils.SystemPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CacheCheckJob {

    @Resource
    private MemoryCacheService memoryCacheService;

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void analyzeUserPreferences() {
        // 这里可以添加一些定时清理缓存的逻辑，或者其他需要定时执行的任务
        // 例如：清理过期的线程中断信号
        // 目前只是一个示例，可以根据实际需求进行扩展
        SystemPrinter.println("执行定时任务：检查内存缓存");
    }

}
