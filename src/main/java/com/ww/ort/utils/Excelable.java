package com.ww.ort.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface Excelable {

    default void exportModel(HttpServletResponse response, String name){
        ExportExcelUtil exportExcelUtil = new ExportExcelUtil();
        exportExcelUtil.downLoadExcelMould(response, name);
    }

    int importExcel(MultipartFile file) throws Exception;

}
