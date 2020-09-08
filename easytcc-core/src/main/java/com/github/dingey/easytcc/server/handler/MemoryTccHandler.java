package com.github.dingey.easytcc.server.handler;

import com.github.dingey.easytcc.core.Group;
import com.github.dingey.easytcc.core.Status;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author d
 */
@Slf4j
public class MemoryTccHandler extends AbstractHandler implements EasyTccHandler {
    private volatile ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentLinkedQueue<Group>>> groupMap = new ConcurrentHashMap<>();
    private volatile ConcurrentHashMap<String, Integer> groupStatusMap = new ConcurrentHashMap<>();

    @Override
    public int trying(Group group) {
        ConcurrentHashMap<String, ConcurrentLinkedQueue<Group>> groups = groupMap.get(group.getGroupId());

        if (groups == null) {
            groups = new ConcurrentHashMap<>();
            groupMap.put(group.getGroupId(), groups);
        }

        ConcurrentLinkedQueue<Group> xidList = groups.get(group.getXid());
        if (xidList == null) {
            xidList = new ConcurrentLinkedQueue<>();
            groups.put(group.getXid(), xidList);
        }
        xidList.add(group);

        Integer integer = groupStatusMap.get(group.getGroupId());
        if (integer == null || integer < group.getStatus()) {
            groupStatusMap.put(group.getGroupId(), group.getStatus());
        }

        log.debug("trying_{}-> {} : {} : {}", Status.name(group.getStatus()), group.getGroupId(), group.getXid(), group.getStatus());
        cleanIfCompleted(group.getGroupId());

        return 1;
    }

    private void cleanIfCompleted(String groupId) {

        boolean completed = true;
        ConcurrentHashMap<String, ConcurrentLinkedQueue<Group>> xidMap = groupMap.get(groupId);
        for (Map.Entry<String, ConcurrentLinkedQueue<Group>> entry : xidMap.entrySet()) {
            if (entry.getValue().size() != 2) {
                completed = false;
                break;
            }
        }
        if (completed) {
            notifyResult(groupId);
            log.debug("清除{}", groupId);
            groupMap.remove(groupId);
            groupStatusMap.remove(groupId);
        }

    }

    private void notifyResult(String groupId) {
        Integer status = groupStatusMap.get(groupId);
        if (Status.TRYING_FAIL.ordinal() == status) {
            log.debug("rollback group -> {}", groupId);
            ConcurrentHashMap<String, ConcurrentLinkedQueue<Group>> xidMap = groupMap.get(groupId);
            for (Map.Entry<String, ConcurrentLinkedQueue<Group>> entry : xidMap.entrySet()) {
                for (Group g : entry.getValue()) {
                    if (g.getStatus() == Status.TRYING_SUCCESS.ordinal()) {
                        log.debug("rollback branch -> {}:{}", groupId, g.getXid());
                        g.setStatus(Status.CANCEL.ordinal());
                        sendAsync(g);
                    }
                }
            }
        } else if (Status.TRYING_SUCCESS.ordinal() == status) {
            log.debug("confirm group -> {}", groupId);
            ConcurrentHashMap<String, ConcurrentLinkedQueue<Group>> xidMap = groupMap.get(groupId);
            for (Map.Entry<String, ConcurrentLinkedQueue<Group>> entry : xidMap.entrySet()) {
                for (Group g : entry.getValue()) {
                    if (g.getStatus() == Status.TRYING_SUCCESS.ordinal()) {
                        log.debug("confirm branch -> {}:{}", groupId, g.getXid());
                        g.setStatus(Status.CONFIRM.ordinal());
                        sendAsync(g);
                    }
                }
            }
        }

    }
}
