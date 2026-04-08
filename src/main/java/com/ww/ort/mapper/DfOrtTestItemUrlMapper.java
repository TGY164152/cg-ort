package com.ww.ort.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ww.ort.entity.DfOrtTestItemUrl;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author zhao
 * @since 2023-06-28
 */
public interface DfOrtTestItemUrlMapper extends BaseMapper<DfOrtTestItemUrl> {

    @Select("with process_temp as (\n" +
            "\tSELECT process\n" +
            "\tfrom df_ort_test_item_url\n" +
            " ${ew.customSqlSegment} " +
            "\tgroup by process\n" +
            ")\n" +
            "select pt.process\n" +
            "from process_temp pt\n" +
            "inner join df_ort_process dop\n" +
            "on dop.process = pt.process and dop.is_use = 1 \n" +
            "order by dop.sort")
    List<DfOrtTestItemUrl> getBigScreenProcess(@Param(Constants.WRAPPER) Wrapper<DfOrtTestItemUrl> wrapper);
}
