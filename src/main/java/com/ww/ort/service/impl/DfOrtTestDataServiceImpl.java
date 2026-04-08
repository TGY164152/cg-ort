package com.ww.ort.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.ww.ort.entity.DfOrtTestData;
import com.ww.ort.mapper.DfOrtTestDataMapper;
import com.ww.ort.service.DfOrtTestDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * ORT测试数据 服务实现类
 * </p>
 *
 * @author TGY
 * @since 2025-10-11
 */
@Service
public class DfOrtTestDataServiceImpl extends ServiceImpl<DfOrtTestDataMapper, DfOrtTestData> implements DfOrtTestDataService {

    @Autowired
    private DfOrtTestDataMapper dfOrtTestDataMapper;


    @Override
    public List<Map<String,String>> getBatchDataList(Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap) {
        return dfOrtTestDataMapper.getBatchDataList(wrapper, sqlParamMap);
    }

    @Override
    public List<Map<String,String>> getBatchArrayDataList(Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap) {
        return dfOrtTestDataMapper.getBatchArrayDataList(wrapper, sqlParamMap);
    }

    @Override
    public List<Map<String, String>> getThroughBatchDataList(Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap) {
        return dfOrtTestDataMapper.getThroughBatchDataList(wrapper, sqlParamMap);
    }

    @Override
    public List<Map<String, String>> getThroughBatchArrayDataList(Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap) {
        return dfOrtTestDataMapper.getThroughBatchArrayDataList(wrapper, sqlParamMap);
    }
}
