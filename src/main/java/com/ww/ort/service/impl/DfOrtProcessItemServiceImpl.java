package com.ww.ort.service.impl;

import com.ww.ort.entity.DfOrtProcessItem;
import com.ww.ort.mapper.DfOrtProcessItemMapper;
import com.ww.ort.service.DfOrtProcessItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 工序-测试项配置 服务实现类
 * </p>
 *
 * @author TGY
 * @since 2025-10-24
 */
@Service
public class DfOrtProcessItemServiceImpl extends ServiceImpl<DfOrtProcessItemMapper, DfOrtProcessItem> implements DfOrtProcessItemService {

    @Autowired
    private DfOrtProcessItemMapper dfOrtProcessItemMapper;
}
