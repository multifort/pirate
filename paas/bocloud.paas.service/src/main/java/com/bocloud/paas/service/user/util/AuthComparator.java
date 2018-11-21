package com.bocloud.paas.service.user.util;

import java.util.Comparator;

import com.bocloud.paas.model.AuthBean;

public class AuthComparator implements Comparator<AuthBean> {

	@Override
	public int compare(AuthBean first, AuthBean second) {
		if (null == first || second == null) {
			return 0;
		}
		return first.getPriority().compareTo(second.getPriority());
	}

}
