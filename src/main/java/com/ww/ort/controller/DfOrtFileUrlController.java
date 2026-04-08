package com.ww.ort.controller;


import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ww.ort.entity.DfOrtFileUrl;
import com.ww.ort.entity.DfOrtTestData;
import com.ww.ort.entity.DfOrtTestItemImportConfig;
import com.ww.ort.service.DfOrtFileUrlService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * ORT文件路径 前端控制器
 * </p>
 *
 * @author TGY
 * @since 2025-10-28
 */
@Controller
@RequestMapping("/dfOrtFileUrl")
@Api(tags = "ORT文件路径")
@ResponseBody
@CrossOrigin
public class DfOrtFileUrlController {

    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtFileUrlService dfOrtFileUrlService;

    @Transactional(rollbackFor = Exception.class)
    @ApiOperation("导入ORT数据")
    @PostMapping("/ortUpload")
    public Result ortUpload(
            @ApiParam("工厂")@RequestParam(required = true) String factory
            , @ApiParam("型号")@RequestParam(required = true) String project
            , @ApiParam("颜色")@RequestParam(required = true) String color
            , @ApiParam("阶段")@RequestParam(required = true) String stage
            , @ApiParam("工序")@RequestParam(required = true) String process
            , @ApiParam("测试项")@RequestParam(required = true) String checkItem
            , @ApiParam("日期")@RequestParam(required = true) String checkDate
            , @ApiParam("班次")@RequestParam(required = true) String dayOrNight
            , @ApiParam("创建人")@RequestParam(required = true) String createUser
            , @ApiParam("创建人工号")@RequestParam(required = true) String createUsername
            , @ApiParam("文件")@RequestParam(required = true) MultipartFile file
    ) throws Exception {
        //测试时间
        String checkTime = checkDate + " 11:00:00";
        if ("B".equals(dayOrNight)){
            checkTime = checkDate + " 23:00:00";
        }
        Timestamp checkTimeStamp = Timestamp.valueOf(checkTime);

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem);

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (configList == null || configList.size() == 0) {
            return new Result(500, "未找到对应的测试项导入配置");
        }

        ExcelImportUtil excel = new ExcelImportUtil(file);
        //获取该文件所有图片
        Map<String, XSSFPictureData> positionResPic = ExcelImg.getPicturesByMultipartFile(file);

        //上一个批次号
        String lastBatch = "";
        QueryWrapper<DfOrtFileUrl> fileUrlQw = new QueryWrapper<>();
        fileUrlQw
                .eq("factory", factory)
                .eq("project", project)
                .eq("color", color)
                .eq("stage", stage)
                .eq("process", process)
                .eq("check_item", checkItem)
                .eq("check_date", checkDate)
                .eq("day_or_night", dayOrNight)
                .orderByDesc("batch")
                .last("limit 1");
        DfOrtFileUrl fileUrlData = dfOrtFileUrlService.getOne(fileUrlQw);
        if (fileUrlData != null){
            lastBatch = fileUrlData.getBatch();
        }
        //批次号
        String batch = StringUtil.getBatch(lastBatch, checkDate, dayOrNight);

        Map<String,List<Object>> dataMap = new HashMap<>();

        for (DfOrtTestItemImportConfig configData : configList){
            String checkCode = configData.getCheckCode();
            String rowCol = configData.getRowCol();
            String fieldType = configData.getFieldType();
            List<Object> dataList = null;

            if (StringUtils.isBlank(checkCode) || (!rowCol.contains(":") && !rowCol.contains(","))){
                return new Result(200, "配置获取excel范围值的字符串不合法，例如 E5:E9或者E5,E9");
            }

            dataList = ExcelImportUtil.readExcelValuesByFieldType(excel, positionResPic, rowCol, fieldType, env);

            if (dataList == null || dataList.size() == 0){
                return new Result(500, MessageFormat.format("excel文档中{0}上存在空值，无法导入",rowCol));
            }

            dataMap.put(checkCode, dataList);
        }

        List<DfOrtTestData> testDataList = DataUtil.mapToObjects(dataMap, DfOrtTestData.class);
        if (testDataList == null || testDataList.size() == 0){
            return new Result(500, "导入的文件没有相关数据，无法导入");
        }

        DfOrtFileUrl fileUrlDataSave = new DfOrtFileUrl();
        String directory = env.getProperty("filePath");
        String filename = UUID.randomUUID().toString();
        String filePath = FileUtil.saveFile(file, directory, filename);

        fileUrlDataSave.setBigProject("Bare glass");
        fileUrlDataSave.setFactory(factory);
        fileUrlDataSave.setProject(project);
        fileUrlDataSave.setColor(color);
        fileUrlDataSave.setStage(stage);
        fileUrlDataSave.setProcess(process);
        fileUrlDataSave.setCheckItem(checkItem);
        fileUrlDataSave.setCheckTime(checkTimeStamp);
        fileUrlDataSave.setCheckDate(checkDate);
        fileUrlDataSave.setDayOrNight(dayOrNight);
        fileUrlDataSave.setBatch(batch);
        fileUrlDataSave.setFileUrl(filePath.replace(env.getProperty("filePath"), "#filePath#"));
        fileUrlDataSave.setCreateUser(createUser);
        fileUrlDataSave.setCreateUsername(createUsername);
        dfOrtFileUrlService.save(fileUrlDataSave);

        for (DfOrtTestData testData : testDataList){
            testData.setFileId(fileUrlDataSave.getId());
            testData.setFactory(factory);
            testData.setProject(project);
            testData.setColor(color);
            testData.setStage(stage);
            testData.setProcess(process);
            testData.setCheckItem(checkItem);
            testData.setCheckTime(checkTimeStamp);
            testData.setCheckDate(checkDate);
            testData.setDayOrNight(dayOrNight);
            testData.setBatch(batch);
        }

        if (testDataList.size() > 0){
            dfOrtTestDataService.saveBatch(testDataList);
        }

        return  new Result(200, MessageFormat.format("上传ORT数据成功,新增{0}条", testDataList.size()));
    }

    /**
     * 获取列表
     * @param page
     * @param limit
     * @param factory
     * @param project
     * @param color
     * @param stage
     * @param process
     * @param startDate
     * @param endDate
     * @param checkItem
     * @return
     */
    @GetMapping("/listBySearch")
    @ApiOperation("获取列表")
    public Result listBySearch(
            @ApiParam("页码")@RequestParam(value = "page", required = true) Integer page
            , @ApiParam("条数")@RequestParam(value = "limit", required = true) Integer limit
            , @ApiParam("工厂")@RequestParam(value = "factory", required = false) String factory
            , @ApiParam("型号")@RequestParam(value = "project", required = false) String project
            , @ApiParam("颜色")@RequestParam(value = "color", required = false) String color
            , @ApiParam("阶段")@RequestParam(value = "stage", required = false) String stage
            , @ApiParam("工序")@RequestParam(value = "process", required = false) String process
            , @ApiParam("开始日期")@RequestParam(value = "startDate", required = true) String startDate
            , @ApiParam("结束日期")@RequestParam(value = "endDate", required = true) String endDate
            , @ApiParam("测试项")@RequestParam(value = "checkItem", required = true) String checkItem
    ){
        IPage<DfOrtFileUrl> pages = new Page<>(page,limit);

        String startTime =  startDate + " 00:00:00";
        String endTime =  endDate + " 23:59:59";

        QueryWrapper<DfOrtFileUrl> qw = new QueryWrapper<>();

        qw
                .eq(StringUtils.isNotBlank(factory),"factory",factory)
                .eq(StringUtils.isNotBlank(project),"project",project)
                .eq(StringUtils.isNotBlank(color),"color",color)
                .eq(StringUtils.isNotBlank(stage),"stage",stage)
                .eq(StringUtils.isNotBlank(process),"process",process)
                .ge(StringUtils.isNotBlank(startDate),"check_time",startTime)
                .le(StringUtils.isNotBlank(endDate),"check_time",endTime)
                .eq(StringUtils.isNotBlank(checkItem),"check_item",checkItem)
                .orderByDesc("id");

        IPage<DfOrtFileUrl> list = dfOrtFileUrlService.page(pages,qw);

        for (DfOrtFileUrl data : list.getRecords()){
            data.setFileUrl(data.getFileUrl().replace("#filePath#", env.getProperty("fileUrl")));
        }

        return new Result(0,"查询成功",list.getRecords(),(int)list.getTotal());
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete")
    @ApiOperation("删除")
    public Result delete(
            @ApiParam("id") @RequestParam(value = "id", required = true) Integer id
    ){
        if (dfOrtFileUrlService.removeById(id)){
            QueryWrapper<DfOrtTestData> testDataqw = new QueryWrapper<>();
            testDataqw.eq("file_id",id);
            dfOrtTestDataService.remove(testDataqw);
            return new Result(200,"删除成功，同时删除相关文件数据");
        }
        return new Result(500,"删除失败");
    }

    /**
     * 删除
     * @param ids
     * @return
     */
    @GetMapping("/batchDelete")
    @ApiOperation("批量删除信息")
    public Result batchDelete(
            @ApiParam("ids") @RequestParam(value = "ids", required = true) List<Integer> ids
    ){
        if (dfOrtFileUrlService.removeByIds(ids)){
            QueryWrapper<DfOrtTestData> testDataqw = new QueryWrapper<>();
            testDataqw.in("file_id",ids);
            dfOrtTestDataService.remove(testDataqw);
            return new Result(200,"删除成功，同时删除相关文件数据");
        }
        return new Result(500,"删除失败");
    }

    /**
     * 获取文件流
     * @param fileUrl 文件路径
     * @return
     */
    @CrossOrigin(origins = "*")
    @GetMapping("/getFileStream2")
    @ApiOperation("获取文件流")
    public ResponseEntity<Resource> getFileStream2(
            @ApiParam("文件路径") @RequestParam(value = "fileUrl", required = true) String fileUrl
    ){

        String filePath = fileUrl.replace(env.getProperty("fileUrl"), env.getProperty("filePath"));

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // 返回文件流
            Resource resource = new FileSystemResource(file);
            String filename = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream"); // 二进制流
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * 获取Excel文件的PDF文件
     * @param fileUrl
     * @return
     */
    @ApiOperation("获取Excel文件的PDF文件")
        @GetMapping("/getExcelPDF")
    public String getExcelPDF(
            @ApiParam("文件路径") @RequestParam(required = true) String fileUrl
    ) throws Exception {

        String filePath = fileUrl.replace(env.getProperty("fileUrl"), env.getProperty("filePath"));
        String pdfPath = filePath.replace(".xlsx", ".pdf");
        Workbook workbook = new Workbook(filePath);

        PdfSaveOptions options = new PdfSaveOptions();
        options.setAllColumnsInOnePagePerSheet(true);
        options.setOnePagePerSheet(true);

        workbook.save(new FileOutputStream(pdfPath), options);

        String pdfUrl = pdfPath.replace(env.getProperty("filePath"), env.getProperty("fileUrl"));
        return pdfUrl;
    }

    /**
     * 批量导出
     * @param ids
     * @return
     */
    @GetMapping("/batchExport")
    @ApiOperation("批量导出")
    public Result mergeExport(
            @ApiParam("ids") @RequestParam(required = true) List<Integer> ids
    ){
        QueryWrapper<DfOrtFileUrl> qw = new QueryWrapper<>();
        qw.in("id",ids);
        List<DfOrtFileUrl> list = dfOrtFileUrlService.list(qw);

        if (list.size() == 0){
            return new Result(500,"当前条件下没有数据可导出");
        }

        list.stream().forEach( data -> {
            data.setFileUrl(data.getFileUrl().replace("#filePath#", env.getProperty("fileUrl")));
        });

        return new Result(200,"查询成功",list);
    }

    /**
     * 合并导出
     * @param ids
     * @return
     */
    @GetMapping("/mergeExport")
    @ApiOperation("合并导出")
    public Result batchExport(
            @ApiParam("ids") @RequestParam(required = true) List<Integer> ids
    ) throws IOException {
        //需要合并的文件
        List<Map<String, String>> excelFileList = new ArrayList<>();

        QueryWrapper<DfOrtFileUrl> qw = new QueryWrapper<>();
        qw.in("id",ids);
        List<DfOrtFileUrl> list = dfOrtFileUrlService.list(qw);

        if (list.size() == 0){
            return new Result(500,"当前条件下没有数据可导出");
        }

        list.stream().forEach( data -> {
            Map<String,String> excelFileMap = new HashMap<>();
            String sheetName = data.getColor() + "_" + data.getProcess() + "_" + data.getCheckItem() + "_" + data.getBatch();

            excelFileMap.put("sheetName", sheetName);
            excelFileMap.put("srcPath",data.getFileUrl().replace("#filePath#", env.getProperty("fileUrl")));
            excelFileList.add(excelFileMap);
        });

        String filename = UUID.randomUUID().toString() + ".xlsx";
        String outFilePath = env.getProperty("filePath") + filename;

        ExportExcelUtil.mergeExcels(excelFileList, outFilePath);

        return new Result(200,"查询成功", outFilePath);
    }

}
