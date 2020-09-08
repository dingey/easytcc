package com.github.dingey.easytcc.client;

import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * @author d
 */
public class EasyTccContext {
	private static ThreadLocal<String> group = new InheritableThreadLocal<>();
	private static ThreadLocal<String> xid = new InheritableThreadLocal<>();

	public static void setGroup(String groupId) {
		EasyTccContext.group.set(groupId);
	}

	public static void setXid(String xid) {
		EasyTccContext.xid.set(xid);
	}

	public static String getGroupId() {
		return group.get();
	}

	public static String getXid() {
		String s = xid.get();
		if (s == null && getGroupId() != null) {
			s = UUID.randomUUID().toString().replaceAll("-", "");
			setXid(s);
		}
		return s;
	}

	public static void clear() {
		group.remove();
		xid.remove();
	}

	public static boolean isTransation() {
		return StringUtils.hasText(group.get());
	}
}
