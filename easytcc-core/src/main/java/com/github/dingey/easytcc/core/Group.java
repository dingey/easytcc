package com.github.dingey.easytcc.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@SuppressWarnings("unused")
public class Group {
    private String groupId;
    private String xid;
    private int status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TryingInfo trying;

    @Data
    @Accessors(chain = true)
    public static class TryingInfo {
        private String serviceId;

        private String method;

        private String bean;

        private Object[] args;
    }

    public static Group createGroup() {
        Group group = new Group();
        group.generateGroupId();
        group.setXid(group.getGroupId());
        group.setStatus(Status.TRYING_START.ordinal());
        return group;
    }

    public static Group joinGroup(String groupId, String xid) {
        Group group = new Group();
        group.setGroupId(groupId);
        if (xid == null || xid.isEmpty()) {
            group.generateXid();
        } else {
            group.setXid(xid);
        }
        group.setStatus(Status.TRYING_START.ordinal());
        return group;
    }

    public static Group tryingSuccess(String groupId, String xid) {
        Group group = new Group();
        group.setGroupId(groupId);
        group.setXid(xid);
        group.setStatus(Status.TRYING_SUCCESS.ordinal());
        return group;
    }

    public static Group tryingFail(String groupId, String xid) {
        Group group = new Group();
        group.setGroupId(groupId);
        group.setXid(xid);
        group.setStatus(Status.TRYING_FAIL.ordinal());
        return group;
    }

    public Group generateGroupId() {
        this.groupId = UUID.randomUUID().toString().replaceAll("-", "");
        return this;
    }

    public Group generateXid() {
        this.xid = UUID.randomUUID().toString().replaceAll("-", "");
        return this;
    }
}
