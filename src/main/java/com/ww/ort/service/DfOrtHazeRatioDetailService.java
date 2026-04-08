package com.ww.ort.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.ww.ort.entity.DfOrtHazeRatioDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ww.ort.entity.Rate3;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 雾度测量详情 服务类
 * </p>
 *
 * @author zhao
 * @since 2024-12-24
 */
public interface DfOrtHazeRatioDetailService extends IService<DfOrtHazeRatioDetail> {

    int importExcel(String model, String color,String productionPhase, String process, String date, MultipartFile file,String sheetName) throws Exception;

    List<Rate3> getResultSummary(@Param(Constants.WRAPPER) Wrapper<DfOrtHazeRatioDetail> wrapper);

    List<Rate3> listNear6Batch(@Param(Constants.WRAPPER) Wrapper<DfOrtHazeRatioDetail> wrapper);
}
