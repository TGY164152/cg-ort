package com.ww.ort;

import com.ww.ort.utils.ExportExcelUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;

@SpringBootTest
class OrtApplicationTests {

//	@Autowired
//	private DfOrtStandardConfigService dfOrtStandardConfigService;
//
//	@Autowired
//	private DfOrtExperStandConfigService dfOrtExperStandConfigService;
//
//	@Autowired
//	private DfProjectColorService dfProjectColorService;

//	@Test
//	void contextLoads() {
//		List<DfOrtExperStandConfig> list = dfOrtExperStandConfigService.list();
//		for (DfOrtExperStandConfig dfOrtStandardConfig : list) {
//			dfOrtStandardConfig.setId(null);
//			dfOrtStandardConfig.setProject("C98B");
//			dfOrtStandardConfig.setColor("C98B-粉");
//			dfOrtExperStandConfigService.save(dfOrtStandardConfig);
//		}
//		for (DfOrtExperStandConfig dfOrtStandardConfig : list) {
//			dfOrtStandardConfig.setId(null);
//			dfOrtStandardConfig.setProject("C98B");
//			dfOrtStandardConfig.setColor("C98B-透明");
//			dfOrtExperStandConfigService.save(dfOrtStandardConfig);
//		}
////		for (DfProjectColor dfProjectColor : dfProjectColorService.list()) {
////			System.out.println(dfProjectColor);
////		}
//	}

	@Test
	void checkString() {
		System.out.println("4B".compareTo("5B"));
	}


	public static void main(String[] args) {
		List<Map<String, String>> excelFileList = Arrays.asList(
				new HashMap<String, String>() {{
					put("srcPath", "D:\\boen\\ort\\file\\T3PB-LE.xlsx");
					put("sheetName", "C98B-透明_QC1_T3PB-LE_2025-10-28");
				}},
				new HashMap<String, String>() {{
					put("srcPath", "D:\\boen\\ort\\file\\5a7afa15-e36b-4b97-83e4-3701da4e665d.xlsx");
					put("sheetName", "C98B-透明_光油_冷热循环_2025-10-29");
				}},
				new HashMap<String, String>() {{
					put("srcPath", "D:\\boen\\ort\\file\\999.xlsx");
					put("sheetName", "C98B-透明_光油_冷热循环_2025-10-28");
				}}

		);
		String outFilePath = "D:\\boen\\ort\\file\\merge.xlsx";

		try {
			ExportExcelUtil.mergeExcels(excelFileList, outFilePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void mergeExcels() {
		System.out.println(1 * 1.0 / 3);
	}

}
