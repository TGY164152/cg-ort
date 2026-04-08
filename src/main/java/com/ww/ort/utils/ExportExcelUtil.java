package com.ww.ort.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;
import org.springframework.stereotype.Service;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.poi.ss.usermodel.ConditionType.*;
import static org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType.FORMULA;

@Service
public class ExportExcelUtil {

    public void expoerDataExcel(HttpServletResponse response, ArrayList titleKeyList, Map titleMap, List src_list) throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String name = format.format(new Date());
        String xlsFile_name = name+".xlsx"; //输出xls文件名称
        //内存中只创建100个对象
        Workbook wb = new SXSSFWorkbook(100); //关键语句
        Sheet sheet = null; //工作表对象
        Row nRow = null; //行对象
        Cell nCell = null; //列对象
        int rowNo = 0; //总行号
        int pageRowNo = 0; //页行号
        for (int k = 0; k < src_list.size(); k++) {
            Map<String,Object> srcMap = (Map<String, Object>) src_list.get(k);
            //写入300000条后切换到下个工作表
            if (rowNo % 300000 == 0) {
                wb.createSheet("工作簿" + (rowNo / 300000));//创建新的sheet对象
                sheet = wb.getSheetAt(rowNo / 300000); //动态指定当前的工作表
                pageRowNo = 0; //新建了工作表,重置工作表的行号为0
                // -----------定义表头-----------
                nRow = sheet.createRow(pageRowNo++);
                // 列数 titleKeyList.size()
                for (int i = 0; i < titleKeyList.size(); i++) {
                    Cell cell_tem = nRow.createCell(i);
                    cell_tem.setCellValue((String) titleMap.get(titleKeyList.get(i)));
                }
            }
            rowNo++;
            // ---------------------------
            rowNo++;
            nRow = sheet.createRow(pageRowNo++); //新建行对象
            // 行，获取cell值
            for (int j = 0; j < titleKeyList.size(); j++) {
                nCell = nRow.createCell(j);
                if (srcMap.get(titleKeyList.get(j)) != null) {
                    nCell.setCellValue(srcMap.get(titleKeyList.get(j)).toString());
                } else {
                    nCell.setCellValue("");
                }
            }
        }
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + xlsFile_name);
        response.flushBuffer();
        OutputStream outputStream = response.getOutputStream();
        wb.write(response.getOutputStream());
        wb.close();
        outputStream.flush();
        outputStream.close();
    }

    public void downLoadExcelMould(HttpServletResponse response, String excelName) {
        try {
            InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(excelName + ".xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            response.setContentType("application/binary;charset=ISO8859-1");
            String filename = java.net.URLEncoder.encode(excelName, "UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + filename + ".xlsx");
            ServletOutputStream out = null;
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭文件输出流
        }
        return;
    }

    public void liableManMouldExcel(HttpServletResponse response) {
        try {
            InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("责任人模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            response.setContentType("application/binary;charset=ISO8859-1");
            String filename = java.net.URLEncoder.encode("责任人模板", "UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + filename + ".xlsx");
            ServletOutputStream out = null;
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭文件输出流
        }
        return;
    }

    public void workmanshipMouldExcel(HttpServletResponse response) {
        try {
            InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("项目工艺模板.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            response.setContentType("application/binary;charset=ISO8859-1");
            String filename = java.net.URLEncoder.encode("项目工艺模板", "UTF-8");
            response.setHeader("Content-disposition", "attachment; filename=" + filename + ".xlsx");
            ServletOutputStream out = null;
            out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //关闭文件输出流
        }
        return;
    }

    /**
     * 合并多个 Excel 文件到一个文件，每个源文件成为一个 Sheet
     * @param srcFileList 源 Excel 文件数据
     * @param destFilePath 导出目标 Excel 文件路径
     */
    public static void mergeExcels(List<Map<String,String>> srcFileList, String destFilePath) throws IOException {
        try(XSSFWorkbook targetWb = new XSSFWorkbook()) {
            for (Map<String, String> srcFileData : srcFileList) {
                String srcPath = srcFileData.get("srcPath");
                String sheetName = srcFileData.get("sheetName");

                try (FileInputStream fis = new FileInputStream(srcPath);
                     XSSFWorkbook srcWb = new XSSFWorkbook(fis)) {

                    // 清除外部链接
                    if (!srcWb.getExternalLinksTable().isEmpty()) {
                        srcWb.getExternalLinksTable().clear();
                    }

                    XSSFSheet srcSheet = srcWb.getSheetAt(0);
                    XSSFSheet destSheet = targetWb.createSheet(sheetName);

                    copySheet(srcSheet, destSheet, targetWb);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(destFilePath)) {
                targetWb.write(fos);
            }

        }
    }

    /**
     * 完整复制 Sheet（包含：行列内容、单元格样式、合并单元格、图片、页眉页脚、打印设置、冻结/拆分窗格）
     */
    public static void copySheet(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFWorkbook targetWb) {
        if (srcSheet == null || destSheet == null) return;

        XSSFWorkbook srcWb = srcSheet.getWorkbook();
        // 复制行列内容、单元格样式
        copyRowsAndCells(srcSheet, destSheet, targetWb, srcWb);
        // 复制合并单元格
        copyMergedRegions(srcSheet, destSheet);
        // 复制图片
        copyPictures(srcSheet, destSheet, targetWb);
        // 复制页眉页脚
        copyHeaderFooter(srcSheet, destSheet);
        // 复制打印设置和显示设置
        copyPrintAndDisplaySettings(srcSheet, destSheet);
        // 复制条件格式
        copyConditionalFormatting(srcSheet, destSheet);
    }

    /**
     * 复制行列内容和单元格
     */
    private static void copyRowsAndCells(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFWorkbook targetWb, XSSFWorkbook srcWb) {
        //1️⃣ 复制列宽、隐藏状态
        int maxColumn = 0;
        for (int i = srcSheet.getFirstRowNum(); i <= srcSheet.getLastRowNum(); i++) {
            XSSFRow srcRow = srcSheet.getRow(i);
            if (srcRow != null && srcRow.getLastCellNum() > maxColumn) {
                maxColumn = srcRow.getLastCellNum();
            }
        }

        for (int col = 0; col < maxColumn; col++) {
            destSheet.setColumnWidth(col, srcSheet.getColumnWidth(col));
            destSheet.setColumnHidden(col, srcSheet.isColumnHidden(col));
        }

        //2️⃣ 复制默认行高、列宽
        destSheet.setDefaultColumnWidth(srcSheet.getDefaultColumnWidth());
        destSheet.setDefaultRowHeight(srcSheet.getDefaultRowHeight());
        destSheet.setDefaultRowHeightInPoints(srcSheet.getDefaultRowHeightInPoints());

        //3️⃣ 复制行、单元格内容
        for (int i = srcSheet.getFirstRowNum(); i <= srcSheet.getLastRowNum(); i++) {
            XSSFRow srcRow = srcSheet.getRow(i);
            if (srcRow == null) continue;

            // 行高 & 隐藏状态
            XSSFRow destRow = destSheet.createRow(i);
            destRow.setHeight(srcRow.getHeight());

            // 复制单元格
            for (int j = srcRow.getFirstCellNum(); j < srcRow.getLastCellNum(); j++) {
                XSSFCell srcCell = srcRow.getCell(j);
                if (srcCell != null) {
                    XSSFCell destCell = destRow.createCell(j);
                    copyCell(srcCell, destCell, targetWb, srcWb);
                }
            }
        }
    }

    /**
     * 复制单元格内容和样式（包含字体、填充颜色、边框、对齐方式）
     */
    private static void copyCell(XSSFCell srcCell, XSSFCell destCell,
                                 XSSFWorkbook targetWb, XSSFWorkbook srcWb) {
        if (srcCell == null) return;

        XSSFCellStyle srcStyle = srcCell.getCellStyle();
        XSSFCellStyle newStyle = targetWb.createCellStyle();
        newStyle.cloneStyleFrom(srcStyle);

        // 字体克隆
        XSSFFont srcFont = srcWb.getFontAt(srcStyle.getFontIndexAsInt());
        XSSFFont newFont = targetWb.createFont();
        newFont.setBold(srcFont.getBold());
        newFont.setItalic(srcFont.getItalic());
        newFont.setFontName(srcFont.getFontName());
        newFont.setFontHeight(srcFont.getFontHeight());
        newFont.setUnderline(srcFont.getUnderline());
        newFont.setStrikeout(srcFont.getStrikeout());
        if (srcFont.getXSSFColor() != null) {
            newFont.setColor(new XSSFColor(srcFont.getXSSFColor().getRGB(), null));
        }
        newStyle.setFont(newFont);

        // 填充颜色
        if (srcStyle.getFillForegroundXSSFColor() != null) {
            XSSFColor fillFg = srcStyle.getFillForegroundXSSFColor();
            newStyle.setFillForegroundColor(new XSSFColor(fillFg.getRGB(), null));
            newStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        if (srcStyle.getFillBackgroundXSSFColor() != null) {
            XSSFColor fillBg = srcStyle.getFillBackgroundXSSFColor();
            newStyle.setFillBackgroundColor(new XSSFColor(fillBg.getRGB(), null));
        }

        // 边框颜色
        if (srcStyle.getBorderTop() != BorderStyle.NONE && srcStyle.getTopBorderXSSFColor() != null) {
            newStyle.setBorderTop(srcStyle.getBorderTop());
            newStyle.setTopBorderColor(new XSSFColor(srcStyle.getTopBorderXSSFColor().getRGB(), null));
        }
        if (srcStyle.getBorderBottom() != BorderStyle.NONE && srcStyle.getBottomBorderXSSFColor() != null) {
            newStyle.setBorderBottom(srcStyle.getBorderBottom());
            newStyle.setBottomBorderColor(new XSSFColor(srcStyle.getBottomBorderXSSFColor().getRGB(), null));
        }
        if (srcStyle.getBorderLeft() != BorderStyle.NONE && srcStyle.getLeftBorderXSSFColor() != null) {
            newStyle.setBorderLeft(srcStyle.getBorderLeft());
            newStyle.setLeftBorderColor(new XSSFColor(srcStyle.getLeftBorderXSSFColor().getRGB(), null));
        }
        if (srcStyle.getBorderRight() != BorderStyle.NONE && srcStyle.getRightBorderXSSFColor() != null) {
            newStyle.setBorderRight(srcStyle.getBorderRight());
            newStyle.setRightBorderColor(new XSSFColor(srcStyle.getRightBorderXSSFColor().getRGB(), null));
        }

        // 对齐方式
        newStyle.setAlignment(srcStyle.getAlignment());
        newStyle.setVerticalAlignment(srcStyle.getVerticalAlignment());

        destCell.setCellStyle(newStyle);

        // 内容
        switch (srcCell.getCellType()) {
            case STRING:
                destCell.setCellValue(srcCell.getStringCellValue());
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(srcCell)) {
                    destCell.setCellValue(srcCell.getDateCellValue());
                } else {
                    destCell.setCellValue(srcCell.getNumericCellValue());
                }
                break;
            case BOOLEAN:
                destCell.setCellValue(srcCell.getBooleanCellValue());
                break;
            case FORMULA:
                destCell.setCellFormula(srcCell.getCellFormula());
                break;
            case BLANK:
                destCell.setBlank();
                break;
            default:
                break;
        }
    }

    /**
     * 复制合并单元格
     */
    private static void copyMergedRegions(XSSFSheet srcSheet, XSSFSheet destSheet) {
        int numMerged = srcSheet.getNumMergedRegions();
        for (int i = 0; i < numMerged; i++) {
            CellRangeAddress merged = srcSheet.getMergedRegion(i);
            destSheet.addMergedRegion(merged);
        }
    }

    /**
     * 复制图片
     */
    private static void copyPictures(XSSFSheet srcSheet, XSSFSheet destSheet, XSSFWorkbook targetWb) {
        XSSFDrawing drawing = srcSheet.getDrawingPatriarch();
        if (drawing == null) return;

        XSSFDrawing destDrawing = destSheet.createDrawingPatriarch();
        for (XSSFShape shape : drawing.getShapes()) {
            if (shape instanceof XSSFPicture) {
                XSSFPicture picture = (XSSFPicture) shape;
                XSSFClientAnchor anchor = (XSSFClientAnchor) picture.getAnchor();
                XSSFPictureData pdata = picture.getPictureData();

                int pictureIndex = targetWb.addPicture(pdata.getData(), pdata.getPictureType());
                XSSFClientAnchor newAnchor = new XSSFClientAnchor(
                        anchor.getDx1(), anchor.getDy1(),
                        anchor.getDx2(), anchor.getDy2(),
                        anchor.getCol1(), anchor.getRow1(),
                        anchor.getCol2(), anchor.getRow2()
                );
                destDrawing.createPicture(newAnchor, pictureIndex);
            }
        }
    }

    /**
     * 复制页眉页脚
     */
    private static void copyHeaderFooter(XSSFSheet srcSheet, XSSFSheet destSheet) {
        destSheet.getHeader().setLeft(srcSheet.getHeader().getLeft());
        destSheet.getHeader().setCenter(srcSheet.getHeader().getCenter());
        destSheet.getHeader().setRight(srcSheet.getHeader().getRight());

        destSheet.getFooter().setLeft(srcSheet.getFooter().getLeft());
        destSheet.getFooter().setCenter(srcSheet.getFooter().getCenter());
        destSheet.getFooter().setRight(srcSheet.getFooter().getRight());
    }

    /**
     * 复制打印设置和显示设置
     */
    private static void copyPrintAndDisplaySettings(XSSFSheet srcSheet, XSSFSheet destSheet) {
        destSheet.setAutobreaks(srcSheet.getAutobreaks());
        destSheet.setDefaultColumnWidth(srcSheet.getDefaultColumnWidth());
        destSheet.setDefaultRowHeight(srcSheet.getDefaultRowHeight());
        destSheet.setDisplayGridlines(srcSheet.isDisplayGridlines());
        destSheet.setDisplayFormulas(srcSheet.isDisplayFormulas());
        destSheet.setDisplayRowColHeadings(srcSheet.isDisplayRowColHeadings());
        destSheet.setHorizontallyCenter(srcSheet.getHorizontallyCenter());
        destSheet.setVerticallyCenter(srcSheet.getVerticallyCenter());
    }

    /**
     * 复制条件格式（Conditional Formatting）
     * 适用于 JDK 8 与 Apache POI 5.x+
     */
    private static void copyConditionalFormatting(XSSFSheet srcSheet, XSSFSheet destSheet) {
        XSSFSheetConditionalFormatting srcCF = srcSheet.getSheetConditionalFormatting();
        XSSFSheetConditionalFormatting destCF = destSheet.getSheetConditionalFormatting();

        for (int i = 0; i < srcCF.getNumConditionalFormattings(); i++) {
            XSSFConditionalFormatting srcFormatting = srcCF.getConditionalFormattingAt(i);
            if (srcFormatting == null) {
                continue;
            }

            // === 复制应用区域 ===
            CellRangeAddress[] srcRanges = srcFormatting.getFormattingRanges();
            CellRangeAddress[] destRanges = new CellRangeAddress[srcRanges.length];
            for (int j = 0; j < srcRanges.length; j++) {
                destRanges[j] = srcRanges[j].copy();
            }

            // === 复制每个规则 ===
            List<XSSFConditionalFormattingRule> newRules = new ArrayList<XSSFConditionalFormattingRule>();

            for (int r = 0; r < srcFormatting.getNumberOfRules(); r++) {
                XSSFConditionalFormattingRule srcRule = srcFormatting.getRule(r);
                XSSFConditionalFormattingRule destRule = null;

                // === 根据条件类型创建新规则 ===
                ConditionType type = srcRule.getConditionType();

                if (type == ConditionType.CELL_VALUE_IS) {
                    destRule = destCF.createConditionalFormattingRule(
                            srcRule.getComparisonOperation(),
                            srcRule.getFormula1(),
                            srcRule.getFormula2()
                    );
                } else if (type == ConditionType.FORMULA) {
                    destRule = destCF.createConditionalFormattingRule(srcRule.getFormula1());
                } else if (type == ConditionType.COLOR_SCALE) {
                    destRule = destCF.createConditionalFormattingColorScaleRule();
                } else {
                    // 其他类型暂不处理
                    continue;
                }

                if (destRule == null) {
                    continue;
                }

                // === 复制样式（字体、边框、背景等）===
                copyConditionalFormattingStyle(srcRule, destRule);

                newRules.add(destRule);
            }

            // === 添加规则到目标 Sheet ===
            if (!newRules.isEmpty()) {
                XSSFConditionalFormattingRule[] rulesArray =
                        new XSSFConditionalFormattingRule[newRules.size()];
                newRules.toArray(rulesArray);
                destCF.addConditionalFormatting(destRanges, rulesArray);
            }
        }
    }


    /**
     * 复制条件格式规则样式（字体、边框、填充、色阶、数据条、图标集等）
     * JDK 8 兼容写法
     */
    private static void copyConditionalFormattingStyle(
            XSSFConditionalFormattingRule srcRule,
            XSSFConditionalFormattingRule destRule
    ) {
        // === 字体格式 ===
        FontFormatting srcFontFmt = srcRule.getFontFormatting();
        if (srcFontFmt != null) {
            FontFormatting destFontFmt = destRule.createFontFormatting();
            destFontFmt.setFontStyle(srcFontFmt.isItalic(), srcFontFmt.isBold());
            destFontFmt.setUnderlineType(srcFontFmt.getUnderlineType());
            destFontFmt.setEscapementType(srcFontFmt.getEscapementType());

            if (srcFontFmt instanceof XSSFFontFormatting && destFontFmt instanceof XSSFFontFormatting) {
                XSSFFontFormatting xssfSrcFontFmt = (XSSFFontFormatting) srcFontFmt;
                XSSFFontFormatting xssfDestFontFmt = (XSSFFontFormatting) destFontFmt;
                XSSFColor fontColor = xssfSrcFontFmt.getFontColor();
                if (fontColor != null) {
                    xssfDestFontFmt.setFontColor(fontColor);
                }
            }
        }

        // === 边框格式 ===
        BorderFormatting srcBorderFmt = srcRule.getBorderFormatting();
        if (srcBorderFmt != null) {
            BorderFormatting destBorderFmt = destRule.createBorderFormatting();
            destBorderFmt.setBorderBottom(srcBorderFmt.getBorderBottom());
            destBorderFmt.setBorderTop(srcBorderFmt.getBorderTop());
            destBorderFmt.setBorderLeft(srcBorderFmt.getBorderLeft());
            destBorderFmt.setBorderRight(srcBorderFmt.getBorderRight());
            destBorderFmt.setBottomBorderColor(srcBorderFmt.getBottomBorderColor());
            destBorderFmt.setTopBorderColor(srcBorderFmt.getTopBorderColor());
            destBorderFmt.setLeftBorderColor(srcBorderFmt.getLeftBorderColor());
            destBorderFmt.setRightBorderColor(srcBorderFmt.getRightBorderColor());
        }

        // === 填充样式 ===
        PatternFormatting srcPatternFmt = srcRule.getPatternFormatting();
        if (srcPatternFmt != null) {
            PatternFormatting destPatternFmt = destRule.createPatternFormatting();
            destPatternFmt.setFillPattern(srcPatternFmt.getFillPattern());
            destPatternFmt.setFillBackgroundColor(srcPatternFmt.getFillBackgroundColor());
            destPatternFmt.setFillForegroundColor(srcPatternFmt.getFillForegroundColor());

            if (srcPatternFmt instanceof XSSFPatternFormatting && destPatternFmt instanceof XSSFPatternFormatting) {
                XSSFPatternFormatting xssfSrcPatternFmt = (XSSFPatternFormatting) srcPatternFmt;
                XSSFPatternFormatting xssfDestPatternFmt = (XSSFPatternFormatting) destPatternFmt;
                XSSFColor fg = xssfSrcPatternFmt.getFillForegroundColorColor();
                XSSFColor bg = xssfSrcPatternFmt.getFillBackgroundColorColor();
                if (fg != null) xssfDestPatternFmt.setFillForegroundColor(fg);
                if (bg != null) xssfDestPatternFmt.setFillBackgroundColor(bg);
            }
        }
    }
}