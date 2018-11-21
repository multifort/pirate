package com.bocloud.paas.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.bocloud.common.model.BsmResult;
import com.bocloud.common.model.Result;
import com.bocloud.common.ssh.CommandExcutor;
import com.bocloud.common.ssh.SSH;

public class FileUtil {

	private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

	/**
	 * 递归删除目录
	 * 
	 * @param dir test
	 * @return
	 */
	public static boolean deleteDirectory(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			if (null == children) {
				return dir.delete();
			}
			// 递归删除目录中的子目录
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDirectory(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	/**
	 * 创建文件夹
	 * 
	 * @param hostIp
	 *            主机ip
	 * @param hostUser
	 *            主机用户
	 * @param hostPwd
	 *            主机密码
	 * @param folder
	 *            需要创建的文件夹
	 * @return
	 */
	public static Result mkdirFolder(String hostIp, String hostUser, String hostPwd, String folder) {
		SSH ssh = CommandExcutor.getSsh(hostIp, hostUser, hostPwd);
		try {
			if (ssh.connect()) {
				String command = "cd; mkdir -p ~/" + folder + "; cd ~/" + folder + "; pwd";
				try {
					String result = ssh.executeWithResult(command);
					return new Result(true, result);
				} catch (Exception e) {
					logger.error("创建文件夹异常：", e);
					return new Result(false, "执行命令：" + command + "异常。");
				}
			}
			return new Result(false, "SSH无法连接目标主机(" + hostIp + ")！请检查主机运行状态、网络连接状态或核对输入正确的用户名和密码。");
		} catch (NullPointerException | IOException npe) {
			logger.error("Get host connection exception：", npe);
			return new Result(false, "SSH无法连接目标主机(" + hostIp + ")！请检查主机运行状态、网络连接状态或核对输入正确的用户名和密码。");
		} finally {
			ssh.close();
		}
	}

	/**
	 * 文件创建 并写入内容
	 * 
	 * @param fileName
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static BsmResult createFile(String fileName, String content) {
		File file = new File(fileName);
		
		file.setWritable(true, false);//设置目录文件权限
		
		logger.info("文件所在的绝对路径："+file.getAbsolutePath());
		
		// 判断目标文件所在的目录是否存在
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs()) {
				logger.warn("创建目标文件所在目录失败："+file.getAbsolutePath());
				return new BsmResult(false, "创建目标文件所在目录失败。");
			}
		}

		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			logger.error("文件内容写入失败", e);
			return new BsmResult(false, "文件内容写入失败。 ");
		}
		return new BsmResult(true, file, "文件内容写入成功。");
	}
	
	/**
	 * 文件创建 并写入内容
	 * 
	 * @param fileName
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public static boolean modifyFile(String fileName, String content) {

		try {
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
			writer.write("");//清除文件内容，重新写入
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			logger.error("文件内容写入失败", e);
			return false;
		}
		return true;
	}

	/**
	 * 读取文件内容
	 * 
	 * @param filePath
	 * @return
	 */
	public static String readFile(String filePath) {
		StringBuilder content = new StringBuilder();
		String encoding = "UTF-8";
		File file = new File(filePath);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				content.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return "";
		}
		return content.toString();
	}
	/**
	 * 读文件内容
	 * @param filePath
	 * @return 返回内容数组，每行为一个String
	 */
	public static List<String> readContent(String filePath) {
		List<String> list = new ArrayList<>();
		String encoding = "UTF-8";
		File file = new File(filePath);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				list.add(lineTxt);
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return list;
		}
		return list;
	}

	/**
	 * 获取文件路径
	 * 
	 * @param fileDir
	 *            文件在resource里的文件目录
	 * @return
	 */
	public static String filePath(String fileDir) {
		try {
			String path = Thread.currentThread().getContextClassLoader().getResource("") + fileDir + File.separatorChar;
			// 判断操作系统类型
			if (System.getProperty("os.name").toUpperCase().contains("windows".toUpperCase())) {
				path = path.replace("file:/", "");
			} else {
				path = path.replace("file:", "");
			}
			return path;
		} catch (Exception e) {
			logger.error("Get fileDir [" + fileDir + "] path exception：", e);
			return "";
		}

	}

	/**
	 * 读取template模板文件内容
	 *
	 * @param file
	 * @return
	 */
	public static String readTemplate(File file) {

		StringBuffer content = new StringBuffer();
		String encoding = "UTF-8";
		try {

			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = new String();
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contentEquals("{\"parameters\":")) {
					break;
				}
				content.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return null;
		}
		return content.toString();
	}

	/**
	 * 读取parameters模板文件内容
	 * 
	 * @param file
	 * @return
	 */
	public static String readParameters(File file) {
		boolean flag = false;
		StringBuilder content = new StringBuilder();
		String encoding = "UTF-8";
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contentEquals("{\"parameters\":")) {
					content.append(lineTxt).append("\n");
					flag = true;
					continue;
				}
				if (flag) {
					content.append(lineTxt).append("\n");
				}
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return null;
		}
		return content.toString();
	}

	/**
	 * @param filePath
	 * @param fileName
	 * @param parameters
	 * @return
	 */
	public static File templateToExecute(String filePath, String fileName, JSONObject parameters, String newFileName) {
		StringBuffer content = new StringBuffer();
		String encode = "UTF-8";
		File file = new File(filePath + fileName);
		// 读取模板文件内容
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encode);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt="";
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contentEquals("{\"parameters\":")) {
					break;
				}
				content.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("read template file exception", e);
			return null;
		}
		// 创建临时文件
		File temporaryFile = new File(filePath + newFileName);
		try {
			FileWriter writer = new FileWriter(temporaryFile);
			BufferedWriter bufferWritter = new BufferedWriter(writer);
			bufferWritter.write(content.toString());
			bufferWritter.close();
			writer.close();
		} catch (IOException e) {
			logger.error("create temporary file exception", e);
			return null;
		}
		// 替换临时文件中的参数为真实的值，构建可执行的文件
		String temporaryFilePath = temporaryFile.getPath();
		FileResourceLoader resourceLoader = new FileResourceLoader(filePath, encode);
		Configuration configuration = null;
		try {
			configuration = Configuration.defaultConfiguration();
			GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
			Template template = groupTemplate.getTemplate(temporaryFile.getName());
			Iterator<String> iterator = parameters.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				template.binding(key, parameters.get(key));
			}
			String templateContent = template.render();
			// 向文件中写入数据，组装成创建存储的模板
			FileWriter writer = new FileWriter(temporaryFilePath);
			writer.write(templateContent);
			writer.close();
		} catch (IOException e) {
			logger.error("template to execute file exception", e);
			return null;
		}
		return temporaryFile;
	}
	/**
	 * 将可执行模板内容写入临时文件中
	 * @param filePath
	 * @param newFileName
	 * @param content
	 * @return
	 */
	public static File createTemporaryFile(String filePath, String newFileName, String content){
		StringBuffer templateContent = new StringBuffer();
		String encode = "UTF-8";
		// 创建临时文件
		File temporaryFile = new File(filePath + newFileName);
		String temporaryFilePath = temporaryFile.getPath();
		try {
			// 向文件中写入数据，组装成创建存储的模板
			FileWriter writer = new FileWriter(temporaryFilePath);
			writer.write(content);
			writer.close();
		} catch (IOException e) {
			logger.error("template content to temporary file exception", e);
			return null;
		}

		// 读取模板文件内容
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(temporaryFile), encode);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = " ";
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contentEquals("{\"parameters\":")) {
					break;
				}
				templateContent.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("read template not include parameters file exception", e);
			return null;
		}
		
		// 向文件中写入数据，组装成创建存储的模板
		try {
			FileWriter writer = new FileWriter(temporaryFilePath);
			writer.write(templateContent.toString());
			writer.close();
		} catch (IOException e) {
			logger.error("excute template content to temporary file exception", e);
			return null;
		}
		
		return temporaryFile;
	}
	
	/**
	 * 模板镜像升级
	 * @param fileName
	 * @param type
	 * @param context
	 * @return
	 */
	public static String imageUpgrade(String fileName, String image, String imageName){
		StringBuffer content = new StringBuffer();
		String encoding = "UTF-8";
		File file = new File(fileName);
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contains("image") && !lineTxt.contains("imagePullPolicy")) {
					lineTxt = lineTxt.split(":")[0] + ": "+imageName;
					content.append(lineTxt).append("\n");
					continue;
				}
				
				content.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
			
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
		}
		
		return content.toString();
	}
	/**
	 * 获取模板里的镜像
	 * @param fileName
	 * @return
	 */
	public static String getTemplateImage(String fileName){
		String encoding = "UTF-8";
		File file = new File(fileName);
		String image = null;
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contains("image") && !lineTxt.contains("imagePullPolicy")) {
					image = lineTxt.split(": ")[1];
					break;
				}
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("Read file contents exception：", e);
			return null;
		}
		return image;
	}

	/**
	 * @param filePath
	 * @param fileName
	 * @param parameters
	 * @return
	 */
	public static File templateToExecuteFile(String filePath, String fileName, JSONObject parameters, String newFileName) {

		StringBuffer content = new StringBuffer();
		String encode = "UTF-8";
		File file = new File(filePath + File.separator + fileName);
		// 读取模板文件内容
		try {
			InputStreamReader read = new InputStreamReader(new FileInputStream(file), encode);
			BufferedReader bufferedReader = new BufferedReader(read);
			String lineTxt;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				if (lineTxt.contentEquals("{\"parameters\":")) {
					break;
				}
				content.append(lineTxt).append("\n");
			}
			bufferedReader.close();
			read.close();
		} catch (IOException e) {
			logger.error("read template file exception", e);
			return null;
		}
		// 创建临时文件
		File temporaryFile = new File(filePath + File.separator + newFileName);

		// 判断临时文件是否存在
		boolean delResult = false;

		try {
			// 如果存在，先删除再创建
			if (temporaryFile.exists()) {
				delResult = deleteDirectory(temporaryFile);
			}else{
				delResult = true;
			}
			// 创建临时文件
			if(delResult){
				boolean createResult = temporaryFile.createNewFile();
				if (!createResult) {
					return null;
				}
				FileWriter writer = new FileWriter(temporaryFile);
				BufferedWriter bufferWritter = new BufferedWriter(writer);
				bufferWritter.write(content.toString());
				bufferWritter.close();
				writer.close();
			}
		} catch (IOException e) {
			logger.error("create template file exception", e);
			return null;
		}

		// 替换临时文件中的参数为真实的值，构建可执行的文件
		String temporaryFilePath = temporaryFile.getPath();
		FileResourceLoader resourceLoader = new FileResourceLoader(filePath, encode);
		try {
			Configuration configuration = Configuration.defaultConfiguration();
			GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
			Template template = groupTemplate.getTemplate(temporaryFile.getName());
			Iterator<String> iterator = parameters.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				template.binding(key, parameters.get(key));
			}
			String templateContent = template.render();

			// 向文件中写入数据，组装成创建存储的模板
			FileWriter writer = new FileWriter(temporaryFilePath);
			writer.write(templateContent);
			writer.close();
		} catch (IOException e) {
			logger.error("template to execute file exception", e);
			return null;
		}
		return temporaryFile;
	}

}
