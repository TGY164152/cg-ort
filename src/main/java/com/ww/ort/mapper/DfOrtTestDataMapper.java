package com.ww.ort.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ww.ort.entity.DfOrtTestData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * ORT测试数据 Mapper 接口
 * </p>
 *
 * @author TGY
 * @since 2025-10-11
 */
public interface DfOrtTestDataMapper extends BaseMapper<DfOrtTestData> {

    @Select("with data_temp as (\n" +
            "\tselect \n" +
            "\t*\n" +
            "\t,dense_rank() over(order by ${sqlParamMap.rowSql} desc) row_num\n" +
            "\tfrom df_ort_test_data \n" +
            " ${ew.customSqlSegment} " +
            ")\n" +
            "select ${sqlParamMap.selectSql}\n" +
            "from data_temp\n" +
            "where row_num <= ${sqlParamMap.numSql}\n" +
            "order by ${sqlParamMap.orderSql}")
    List<Map<String,String>> getBatchDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);

    @Select("with data_temp as (\n" +
            "\tselect \n" +
            "\t*\n" +
            "\t,dense_rank() over(order by ${sqlParamMap.rowSql} desc) row_num\n" +
            "\tfrom df_ort_test_data \n" +
            " ${ew.customSqlSegment} " +
            ")\n" +
            "select ${sqlParamMap.selectSql}\n" +
            "from data_temp\n" +
            "where row_num <= ${sqlParamMap.numSql}\n" +
            "group by ${sqlParamMap.groupSql} \n" +
            "order by ${sqlParamMap.orderSql}")
    List<Map<String,String>> getBatchArrayDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);


    @Select("with data_temp as (\n" +
            "\tselect \n" +
            "\t*\n" +
            "\t,REGEXP_REPLACE(string1, '[^0-9]', '') + 0 AS wave\n" +
            "\t,dense_rank() over(order by ${sqlParamMap.rowSql} desc) row_num\n" +
            "\tfrom df_ort_test_data \n" +
            " ${ew.customSqlSegment} " +
            ")\n" +
            "select " +
            "${sqlParamMap.selectSql}\n" +
            "from data_temp\n" +
            "where row_num <= ${sqlParamMap.numSql}\n" +
            "group by ${sqlParamMap.groupSql} \n" +
            "order by ${sqlParamMap.orderSql}")
    List<Map<String,String>> getThroughBatchDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);


    @Select("with data_temp as ( \n" +
            "\tselect \n" +
            "\t*\n" +
            "\t,REGEXP_REPLACE(string1, '[^0-9]', '') + 0 AS wave\n" +
            "\t,dense_rank() over(order by ${sqlParamMap.rowSql} desc) row_num\n" +
            "\tfrom df_ort_test_data \n" +
            " ${ew.customSqlSegment} " +
            ")\n" +
            ", data_new as (\n" +
            "select " +
            "${sqlParamMap.selectSqlNew}\n" +
            "from data_temp\n" +
            "where row_num <= ${sqlParamMap.numSql}\n" +
            "group by ${sqlParamMap.groupSqlNew} \n" +
            "order by ${sqlParamMap.orderSqlNew} \n" +
            ")\n" +
            "select \n" +
            "${sqlParamMap.selectSql}\n" +
            "from data_new \n" +
            "group by ${sqlParamMap.groupSql} \n" +
            "order by ${sqlParamMap.orderSql}")
    List<Map<String,String>> getThroughBatchArrayDataList(@Param(Constants.WRAPPER) Wrapper<DfOrtTestData> wrapper, Map<String, Object> sqlParamMap);
}
