package com.bocloud.paas.web.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.paas.web.model.Department;
import com.bocloud.paas.web.model.Role;
import com.bocloud.common.enums.BoCloudMethod;
import com.bocloud.common.enums.BoCloudService;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.utils.MapTools;
import com.bocloud.registry.http.core.ServiceFactory;
import com.bocloud.registry.http.model.RemoteService;

public class ExcelReader {

	private static Logger logger = LoggerFactory.getLogger(ExcelReader.class);

	private POIFSFileSystem fs;
	private HSSFWorkbook wb;
	private HSSFSheet sheet;
	private HSSFRow row;

	/**
	 * 读取文件内容
	 * 
	 * @param is
	 * @param serviceFactory
	 * @param SERVICE
	 * @param request
	 * @return
	 */
	public List<String> readExcelContent(InputStream is, ServiceFactory serviceFactory, BoCloudService SERVICE,
			HttpServletRequest request) {
		List<String> result = new ArrayList<>();
		String password = "123456";
		try {
			fs = new POIFSFileSystem(is);
			wb = new HSSFWorkbook(fs);
		} catch (IOException e) {
			logger.error("init POI error:", e);
		}
		sheet = wb.getSheetAt(0);
		// 得到总行数
		int rowNum = sheet.getLastRowNum();
		row = sheet.getRow(0);
		int colNum = row.getPhysicalNumberOfCells();
		String[] rowName = { "userId", "name", "sex", "role", "dept" };
		// 正文内容应该从第二行开始,第一行为表头的标题
		for (int i = 1; i <= rowNum; i++) {
			JSONObject jsonObject = new JSONObject();
			row = sheet.getRow(i);
			for (int j = 0; j < colNum; j++) {
				String value = row.getCell(j).getStringCellValue();
				if (value.equals("1001")) {
					jsonObject.put(rowName[j], true);
				} else if (value.equals("1002")) {
					jsonObject.put(rowName[j], false);
				} else {
					jsonObject.put(rowName[j], value);
				}
			}
			jsonObject.put("username", jsonObject.get("userId"));
			jsonObject.put("password", password);
			// 处理组织机构
			Map<String, Object> paramMap = MapTools.simpleMap("remark", jsonObject.getString("dept"));
			Map<String, Object> param = MapTools.simpleMap("remark", jsonObject.getString("role"));
			RemoteService service = serviceFactory.safeBuild(SERVICE, "/department/remark", BoCloudMethod.OTHERS, null,
					paramMap, request);
			BsmResult bsmResult = service.invoke();
			Department department = null;
			Role role = null;
			if (bsmResult.isSuccess()) {
				department = JSONObject.parseObject(bsmResult.getData().toString(), Department.class);
			}
			// 处理角色
			service = serviceFactory.safeBuild(SERVICE, "/role/remark", BoCloudMethod.OTHERS, null, param, request);
			bsmResult = service.invoke();
			if (bsmResult.isSuccess()) {
				role = JSONObject.parseObject(bsmResult.getData().toString(), Role.class);
			}
			if (null != department && null != role) {
				jsonObject.put("departId", department.getId());
				jsonObject.put("roleId", role.getId());
				result.add(jsonObject.toString());
			}
		}
		return result;
	}
}
