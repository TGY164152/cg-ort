package com.ww.ort.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ww.ort.entity.DfOrtStandardConfig;
import com.ww.ort.mapper.DfOrtStandardConfigMapper;
import com.ww.ort.service.DfOrtStandardConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhao
 * @since 2023-06-19
 */
@Service
public class DfOrtStandardConfigServiceImpl extends ServiceImpl<DfOrtStandardConfigMapper, DfOrtStandardConfig> implements DfOrtStandardConfigService {
    /*获取标准*/
    public Map<String, Map<String, Double>> getStandardConfigByProcess(String project, String color, String process) {
        QueryWrapper<DfOrtStandardConfig> qw = new QueryWrapper<>();
        qw.like("process", process)
                .eq("project", project)
                .eq("color", color);
        Map<String, Double> standMin = new HashMap<>();
        Map<String, Double> standMax = new HashMap<>();
        for (DfOrtStandardConfig dfOrtStandardConfig : list(qw)) {
            standMin.put(dfOrtStandardConfig.getProcess() + dfOrtStandardConfig.getCheckItem(), dfOrtStandardConfig.getStandardMin());
            standMax.put(dfOrtStandardConfig.getProcess() + dfOrtStandardConfig.getCheckItem(), dfOrtStandardConfig.getStandardMax());
        }
        Map<String, Map<String, Double>> result = new HashMap<>();
        result.put("min", standMin);
        result.put("max", standMax);
        return result;
    }
}
