package com.bocloud.paas.model;

import java.util.List;

public class MenuBean {

	private Long id;
	private String name;
	private String actionUrl;
	private String icon;
	private Integer priority;
	private List<AuthBean> children;

	public MenuBean(Long id, String name, String actionUrl, String icon, Integer priority) {
		super();
		this.id = id;
		this.name = name;
		this.actionUrl = actionUrl;
		this.icon = icon;
		this.priority = priority;
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
	 * @return the actionUrl
	 */
	public String getActionUrl() {
		return actionUrl;
	}

	/**
	 * @param actionUrl
	 *            the actionUrl to set
	 */
	public void setActionUrl(String actionUrl) {
		this.actionUrl = actionUrl;
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon
	 *            the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the children
	 */
	public List<AuthBean> getChildren() {
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<AuthBean> children) {
		this.children = children;
	}

}
