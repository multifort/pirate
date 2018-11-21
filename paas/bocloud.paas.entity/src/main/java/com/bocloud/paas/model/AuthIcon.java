package com.bocloud.paas.model;

public class AuthIcon {

	private Long id;

	private String icon;

	public AuthIcon() {

	}

	public AuthIcon(Long id, String icon) {
		super();
		this.id = id;
		this.icon = icon;
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

}
