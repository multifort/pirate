package com.bocloud.paas.model;

public class AuthParentsBean {

	private Long id; // ID
	private String name; // 名称
	private Integer priority; // 优先级

	public AuthParentsBean() {
	}

	public AuthParentsBean(Long id, String name, Integer priority) {
		super();
		this.id = id;
		this.name = name;
		this.priority = priority;
	}

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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

}
