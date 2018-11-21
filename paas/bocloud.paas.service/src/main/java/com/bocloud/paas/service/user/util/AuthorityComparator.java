package com.bocloud.paas.service.user.util;

import java.util.Comparator;

import com.bocloud.paas.entity.Authority;

public class AuthorityComparator implements Comparator<Authority> {

	@Override
	public int compare(Authority first, Authority second) {
		if (null == first || second == null) {
			return 0;
		}
		return first.getPriority().compareTo(second.getPriority());
	}
}
