package com.ww.ort.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ww.ort.entity.DfOrtTestItemUrl;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhao
 * @since 2023-06-28
 */
public interface DfOrtTestItemUrlService extends IService<DfOrtTestItemUrl> {

    List<DfOrtTestItemUrl> getBigScreenProcess(@Param(Constants.WRAPPER) Wrapper<DfOrtTestItemUrl> wrapper);
}
