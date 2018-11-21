package com.bocloud.paas.entity;

import com.alibaba.fastjson.JSONObject;
import com.bocloud.entity.annotations.Column;
import com.bocloud.entity.annotations.PK;
import com.bocloud.entity.annotations.Table;
import com.bocloud.entity.bean.GenericEntity;
import com.bocloud.entity.meta.PKStrategy;

/**
 * 字典实体类
 * 
 * @author luogan
 *
 */
@Table("dictionary")
public class Dictionary extends GenericEntity {

	@PK(value = PKStrategy.AUTO)
	private Long id;
	@Column("pvalue")
	private String pvalue;
	@Column("dict_key")
	private String dictKey;
	@Column("dict_value")
	private String dictValue;
	@Column("software_type")
	private String softwareType;

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the pvalue
	 */
	public String getPvalue() {
		return pvalue;
	}

	/**
	 * @param pvalue
	 *            the pvalue to set
	 */
	public void setPvalue(String pvalue) {
		this.pvalue = pvalue;
	}

	public String getDictKey() {
		return dictKey;
	}

	public void setDictKey(String dictKey) {
		this.dictKey = dictKey;
	}

	public String getDictValue() {
		return dictValue;
	}

	public void setDictValue(String dictValue) {
		this.dictValue = dictValue;
	}

	public String getSoftwareType() {
		return softwareType;
	}

	public void setSoftwareType(String softwareType) {
		this.softwareType = softwareType;
	}

	public Dictionary(Long id, String pvalue, String dictKey, String dictValue) {
		super();
		this.id = id;
		this.pvalue = pvalue;
		this.dictKey = dictKey;
		this.dictValue = dictValue;
	}

	/**
	 * 
	 */
	public Dictionary() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}

}
