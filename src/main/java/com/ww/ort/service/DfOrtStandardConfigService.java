package com.ww.ort.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ww.ort.entity.DfOrtStandardConfig;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhao
 * @since 2023-06-19
 */
public interface DfOrtStandardConfigService extends IService<DfOrtStandardConfig> {
    Map<String, Map<String, Double>> getStandardConfigByProcess(String project, String color, String process);
}
