package com.bocloud.paas.common.enums;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 模版属性描述信息
 * 
 * @author zjm
 *
 */
public enum TemplateAttrDescInfoEnum {

	/**
	 * 属性名，获取属性对应的名字
	 */
	NAME("name", "属性", ""),
	/**
	 * 属性名
	 */
	DISPLAY_NAME("displayName", "名称", ""),
	/**
	 * 属性描述
	 */
	DESC("desc", "描述", ""),
	/**
	 * 属性类型，目前支持字符(string)，镜像集合(images)也就是下拉框，文件（file）类型等
	 */
	TYPE("type", "类型", ""),
	/**
	 * 属性的默认值
	 */
	DEFAULT("default", "默认值", ""),
	/**
	 * 属性的正则表达式，默认为最多16位的大小写字母和数字，[a-zA-Z][a-zA-Z0-9]{15}
	 */
	REGEX("regex", "属性的正则表达式", "[a-zA-Z][a-zA-Z0-9]{15}"),
	/**
	 * 填写错误，正则表达式验证不通过时的提示信息
	 */
	ERROR("error", "错误信息", "");

	/**
	 * 属性的code
	 */
	private String code;
	/**
	 * 属性描述
	 */
	private String desc;
	/**
	 * 其他/备注信息
	 */
	private String remark;

	/**
	 * 根据code获取枚举信息
	 */
	private static final Map<String, TemplateAttrDescInfoEnum> code2TemplateAttrDescInfoEnum;

	static {
		code2TemplateAttrDescInfoEnum = new HashMap<String, TemplateAttrDescInfoEnum>();
		for (TemplateAttrDescInfoEnum templateAttrDescInfoEnum : TemplateAttrDescInfoEnum.values()) {
			code2TemplateAttrDescInfoEnum.put(templateAttrDescInfoEnum.getCode(), templateAttrDescInfoEnum);
		}
	}

	public static TemplateAttrDescInfoEnum fromCode(String code) throws Exception {
		if (code2TemplateAttrDescInfoEnum.get(code) == null) {
			throw new Exception();
		}
		return code2TemplateAttrDescInfoEnum.get(code);
	}

	public static List<String> getCodes() throws Exception {
		List<String> codes = new LinkedList<String>();
		for (TemplateAttrDescInfoEnum templateAttrDescInfoEnum : TemplateAttrDescInfoEnum.values()) {
			codes.add(templateAttrDescInfoEnum.getCode());
		}
		return codes;
	}

	TemplateAttrDescInfoEnum(String code, String desc, String remark) {
		this.code = code;
		this.desc = desc;
		this.remark = remark;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

}
