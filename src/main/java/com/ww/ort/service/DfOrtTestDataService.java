package com.ww.ort.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ww.ort.entity.DfOrtTestData;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ORT测试数据 服务类
 * </p>
 *
 * @author TGY
 * @since 2025-10-11
 */
public interface DfOrtTestDataService extends IService<DfOrtTestData> {

    List<Map<String, String>> getBatchDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);

    List<Map<String,String>> getBatchArrayDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);

    List<Map<String,String>> getThroughBatchDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);

    List<Map<String,String>> getThroughBatchArrayDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);
}
