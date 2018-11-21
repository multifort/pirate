package com.bocloud.paas.service.user.util;

import java.util.Comparator;

import com.bocloud.paas.model.AuthParentsBean;

public class AuthParentsComparator implements Comparator<AuthParentsBean> {

	@Override
	public int compare(AuthParentsBean first, AuthParentsBean second) {
		if (null == first || second == null) {
			return 0;
		}
		return first.getPriority().compareTo(second.getPriority());
	}
}
