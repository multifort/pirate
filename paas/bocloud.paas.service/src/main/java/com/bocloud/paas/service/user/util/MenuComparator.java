package com.bocloud.paas.service.user.util;

import java.util.Comparator;

import com.bocloud.paas.model.MenuBean;

public class MenuComparator implements Comparator<MenuBean> {

	@Override
	public int compare(MenuBean first, MenuBean second) {
		if (null == first || second == null) {
			return 0;
		}
		return first.getPriority().compareTo(second.getPriority());
	}

}
