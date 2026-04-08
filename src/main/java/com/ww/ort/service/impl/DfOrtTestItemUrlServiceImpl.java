package com.ww.ort.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.ww.ort.entity.DfOrtTestData;
import com.ww.ort.entity.DfOrtTestItemUrl;
import com.ww.ort.mapper.DfOrtTestItemUrlMapper;
import com.ww.ort.service.DfOrtTestItemUrlService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zhao
 * @since 2023-06-28
 */
@Service
public class DfOrtTestItemUrlServiceImpl extends ServiceImpl<DfOrtTestItemUrlMapper, DfOrtTestItemUrl> implements DfOrtTestItemUrlService {

    @Autowired
    private DfOrtTestItemUrlMapper dfOrtTestItemUrlMapper;

    @Override
    public List<DfOrtTestItemUrl> getBigScreenProcess(Wrapper < DfOrtTestItemUrl> wrapper) {
        return dfOrtTestItemUrlMapper.getBigScreenProcess(wrapper);
    }
}
