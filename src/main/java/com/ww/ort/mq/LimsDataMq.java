package com.ww.ort.mq;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.reflect.TypeToken;
import com.ww.ort.entity.DfOrtFileUrl;
import com.ww.ort.entity.DfOrtTestData;
import com.ww.ort.entity.DfOrtTestItemImportConfig;
import com.ww.ort.service.DfOrtFileUrlService;
import com.ww.ort.service.DfOrtTestDataService;
import com.ww.ort.service.DfOrtTestItemImportConfigService;
import com.ww.ort.utils.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.*;

@Service
public class LimsDataMq {
    private static final Logger log = LoggerFactory.getLogger(LimsDataMq.class);

    @Autowired
    private Environment env;

    @Autowired
    private DfOrtTestDataService dfOrtTestDataService;

    @Autowired
    private DfOrtTestItemImportConfigService dfOrtTestItemImportConfigService;

    @Autowired
    private DfOrtFileUrlService dfOrtFileUrlService;

//    @JmsListener(destination = "${lab_cg_ort}", containerFactory = "jtJmsListenerContainerFactoryQueue")
//    public void receiveLimsData(String msg) throws Exception {
//        log.info("receiveLimsData Lims推送数据信息获取 start***************************************");
//        log.info("receiveLimsData Lims推送数据信息 msg：{}", msg);
//        String jsonStr = JsonUtil.convertAsciiStrToJson(msg);
//        log.info("receiveLimsData Lims推送数据信息 json：{}", jsonStr);
////
//        //json字符串转砂轮对象
//        Map<String,Object> map = JsonUtil.toObject(jsonStr, new TypeToken<Map<String,Object>>(){});
//        //工厂
//        String factory = (String) map.get("factory");
//        //型号
//        String project = (String) map.get("project");
//        //颜色
//        String color = "无".equals((String) map.get("color")) ? "/" : (String) map.get("color");
//        //生成阶段
//        String stage = (String) map.get("stage");
//        //工序
//        String process = (String) map.get("process");
//        //测试项目
//        String checkItem = (String) map.get("check_item");
//        //测试日期
//        String checkDate = (String) map.get("test_date");
//        //班次
//        String dayOrNight = (String) map.get("day_or_night");
//        //创建人工号
//        String createUser = (String) map.get("tester_number");
//        //创建人名称
//        String createUsername = (String) map.get("tester_name");
//        //数据
//        List<Map<String,Object>> dataList = (List<Map<String,Object>>) map.get("data");
//        //文件名
//        String filename = (String) map.get("file_name");
//        //base64文件字符串
//        String fileBase64 = (String) map.get("file_base64");
//
//        if (CollectionUtils.isEmpty(dataList)){
//            log.info("receiveLimsData 推送的测试数据为空，无法进行记录");
//            return;
//        }
//
//        QueryWrapper<DfOrtTestItemSendConfig> configQw = new QueryWrapper<>();
//        configQw
//                .eq("process", process)
//                .eq("check_item", checkItem);
//
//        List<DfOrtTestItemSendConfig> configList = dfOrtTestItemSendConfigService.list(configQw);
//        if (configList == null || configList.size() == 0) {
//            log.info("receiveLimsData 未找到对应的测试项导入配置");
//            return;
//        }
//
//        QueryWrapper<DfOrtFileUrl> fileUrlQw = new QueryWrapper<>();
//        fileUrlQw
//                .eq("factory", factory)
//                .eq("project", project)
//                .eq("color", color)
//                .eq("stage", stage)
//                .eq("process", process)
//                .eq("check_item", checkItem)
//                .eq("check_date", checkDate)
//                .eq("day_or_night", dayOrNight)
//                .orderByDesc("batch")
//                .last("limit 1");
//        DfOrtFileUrl fileUrlData = dfOrtFileUrlService.getOne(fileUrlQw);
//        if (fileUrlData != null){
//            Integer fileId = fileUrlData.getId();
//            QueryWrapper<DfOrtTestData> testDataQw = new QueryWrapper<>();
//            testDataQw.eq("file_id", fileId);
//            dfOrtTestDataService.remove(testDataQw);
//            dfOrtFileUrlService.removeById(fileId);
//        }
//        //批次号
//        String batch = StringUtil.getBatch("", checkDate, dayOrNight);
//        //测试时间
//        String checkTime = checkDate + " 11:00:00";
//        if ("B".equals(dayOrNight)){
//            checkTime = checkDate + " 23:00:00";
//        }
//        Timestamp checkTimeStamp = Timestamp.valueOf(checkTime);
//
//        //文件流
//        byte[] fileBytes = FileUtil.base64ToBytes(fileBase64);
//        // 模拟从Redis、数据库或其他地方获取的二进制数据
//        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
//        // 转换为 MultipartFile
//        MultipartFile multipartFile = FileUtil.convertBytesToMultipartFile(fileBytes, filename, contentType);
//        //保存文件路径
//        String directory = env.getProperty("filePath");
//        //保存文件名
//        String saveFilename = UUID.randomUUID().toString();
//        //保存文件的绝对路径
//        String filePath = FileUtil.saveFile(multipartFile, directory, saveFilename);
//
//        DfOrtFileUrl fileUrlDataSave = new DfOrtFileUrl();
//        fileUrlDataSave.setBigProject("Bare glass");
//        fileUrlDataSave.setFactory(factory);
//        fileUrlDataSave.setProject(project);
//        fileUrlDataSave.setColor(color);
//        fileUrlDataSave.setStage(stage);
//        fileUrlDataSave.setProcess(process);
//        fileUrlDataSave.setCheckItem(checkItem);
//        fileUrlDataSave.setCheckTime(checkTimeStamp);
//        fileUrlDataSave.setCheckDate(checkDate);
//        fileUrlDataSave.setDayOrNight(dayOrNight);
//        fileUrlDataSave.setBatch(batch);
//        fileUrlDataSave.setFileUrl(filePath.replace(env.getProperty("filePath"), "#filePath#"));
//        fileUrlDataSave.setCreateUser(createUser);
//        fileUrlDataSave.setCreateUsername(createUsername);
//        dfOrtFileUrlService.save(fileUrlDataSave);
//
//        Integer fileId = fileUrlDataSave.getId();
//
//        List<DfOrtTestData> testDataList = new ArrayList<>();
//        for (Map<String,Object> data : dataList) {
//            Map<String,Object> dataMap = new HashMap<>();
//
//            for (DfOrtTestItemSendConfig config : configList){
//                String checkName = config.getCheckName();
//                String checkCode = config.getCheckCode();
//
//                if (!data.containsKey(checkName)){
//                    continue;
//                }
//
//                dataMap.put(checkCode, data.get(checkName));
//            }
//
//            DfOrtTestData saveData = JsonUtil.toObject(dataMap, DfOrtTestData.class);
//            saveData.setFileId(fileId);
//            saveData.setFactory(factory);
//            saveData.setProject(project);
//            saveData.setColor(color);
//            saveData.setStage(stage);
//            saveData.setProcess(process);
//            saveData.setCheckItem(checkItem);
//            saveData.setCheckTime(checkTimeStamp);
//            saveData.setCheckDate(checkDate);
//            saveData.setDayOrNight(dayOrNight);
//            saveData.setBatch(batch);
//            testDataList.add(saveData);
//        }
//        dfOrtTestDataService.saveBatch(testDataList);
//    }


    @JmsListener(destination = "${lab_cg_ort}", containerFactory = "defaultJmsListenerContainerFactoryQueue")
    public void receiveLimsData(String msg) throws Exception {
        log.info("receiveLimsData Lims推送数据信息获取 start***************************************");
        log.info("receiveLimsData Lims推送数据信息 msg：{}", msg);
        String jsonStr = JsonUtil.convertAsciiStrToJson(msg);
        log.info("receiveLimsData Lims推送数据信息 json：{}", jsonStr);
//
        //json字符串转砂轮对象
        Map<String,Object> map = JsonUtil.toObject(jsonStr, new TypeToken<Map<String,Object>>(){});
        //工厂
        String factory = (String) map.get("factory");
        //型号
        String project = (String) map.get("project");
        //颜色
        String color = (String) map.get("color");
        //生成阶段
        String stage = (String) map.get("stage");
        //工序
        String process = (String) map.get("process");
        //测试项目
        String checkItem = (String) map.get("test");
        //测试日期
        String checkDate = (String) map.get("test_date");
        //班次
        String dayOrNight = (String) map.get("day_or_night");
        //创建人工号
        String createUser = (String) map.get("tester_number");
        //创建人名称
        String createUsername = (String) map.get("tester_name");
        //文件名
        String filename = (String) map.get("file_name");
        //base64文件字符串
        String fileBase64 = (String) map.get("file_base64");

        QueryWrapper<DfOrtTestItemImportConfig> configQw = new QueryWrapper<>();
        configQw
                .eq("process", process)
                .eq("check_item", checkItem);

        List<DfOrtTestItemImportConfig> configList = dfOrtTestItemImportConfigService.list(configQw);
        if (configList == null || configList.size() == 0) {
            log.info("receiveLimsData 未找到对应的测试项导入配置");
            return;
        }

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
            Integer fileId = fileUrlData.getId();
            QueryWrapper<DfOrtTestData> testDataQw = new QueryWrapper<>();
            testDataQw.eq("file_id", fileId);
            dfOrtTestDataService.remove(testDataQw);
            dfOrtFileUrlService.removeById(fileId);
        }
        //批次号
        String batch = StringUtil.getBatch("", checkDate, dayOrNight);
        //测试时间
        String checkTime = checkDate + " 11:00:00";
        if ("B".equals(dayOrNight)){
            checkTime = checkDate + " 23:00:00";
        }
        Timestamp checkTimeStamp = Timestamp.valueOf(checkTime);

        //文件流
        byte[] fileBytes = FileUtil.base64ToBytes(fileBase64);
        // 模拟从Redis、数据库或其他地方获取的二进制数据
        String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        // 转换为 MultipartFile
        MultipartFile multipartFile = FileUtil.convertBytesToMultipartFile(fileBytes, filename, contentType);

        ExcelImportUtil excel = new ExcelImportUtil(multipartFile);
        //获取该文件所有图片
        Map<String, XSSFPictureData> positionResPic = ExcelImg.getPicturesByMultipartFile(multipartFile);

        Map<String,List<Object>> dataMap = new HashMap<>();

        for (DfOrtTestItemImportConfig configData : configList){
            String checkCode = configData.getCheckCode();
            String rowCol = configData.getRowCol();
            String fieldType = configData.getFieldType();
            List<Object> dataList = null;

            if (StringUtils.isBlank(checkCode) || (!rowCol.contains(":") && !rowCol.contains(","))){
                log.info("receiveLimsData 配置获取excel范围值的字符串不合法，例如 E5:E9或者E5,E9");
                return;
            }

            dataList = ExcelImportUtil.readExcelValuesByFieldType(excel, positionResPic, rowCol, fieldType, env);
            dataMap.put(checkCode, dataList);
        }

        List<DfOrtTestData> testDataList = DataUtil.mapToObjects(dataMap, DfOrtTestData.class);
        if (testDataList == null || testDataList.size() == 0){
            log.info("receiveLimsData 导入的文件没有相关数据，无法导入");
            return;
        }

        //保存文件路径
        String directory = env.getProperty("filePath");
        //保存文件名
        String saveFilename = UUID.randomUUID().toString();
        //保存文件的绝对路径
        String filePath = FileUtil.saveFile(multipartFile, directory, saveFilename);

        DfOrtFileUrl fileUrlDataSave = new DfOrtFileUrl();
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
    }

}