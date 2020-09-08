package com.github.dingey.easytcc.server.handler;

import com.github.dingey.easytcc.core.Group;
import com.github.dingey.easytcc.core.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisTccHandler extends AbstractHandler {
    @Resource
    private StringRedisTemplate srt;

    private final String PREFIX = "easy:tcc:";
    private final String START_GROUP = PREFIX + "start:";
    private final String FINISH_COUNT = PREFIX + "finish:count:";
    private final String SUCCESS_GROUP = PREFIX + "success:";
    private final String FAIL_STATUS = PREFIX + "fail:";

    private final String CONFIRM_GROUP = PREFIX + "confirm:";
    private final String CANCEL_GROUP = PREFIX + "cancel:";

    private boolean setLock(String groupId, String xid, int stats) {
        Boolean setIfAbsent = srt.opsForValue().setIfAbsent(PREFIX + "lock:" + groupId + ":" + xid + ":" + stats, "1", 10L, TimeUnit.SECONDS);
        return Objects.equals(setIfAbsent, true);
    }

    @Override
    public int trying(Group group) {
        if (!setLock(group.getGroupId(), group.getXid(), group.getStatus())) {
            return 0;
        }
        String json = toJson(group);
        if (group.getStatus() == Status.TRYING_START.ordinal()) {
            srt.opsForList().leftPush(START_GROUP + group.getGroupId(), json);
        } else if (group.getStatus() == Status.TRYING_FAIL.ordinal()) {
            srt.opsForValue().increment(FINISH_COUNT + group.getGroupId(), 1);
            srt.opsForValue().increment(FAIL_STATUS + group.getGroupId() + ":" + group.getStatus(), 1);
            //已结束
            if (finished(group)) {
                doFinish(group.getGroupId());
            }
        } else if (group.getStatus() == Status.TRYING_SUCCESS.ordinal()) {
            srt.opsForList().leftPush(SUCCESS_GROUP + group.getGroupId(), json);
            srt.opsForValue().increment(FINISH_COUNT + group.getGroupId(), 1);

            //已结束
            if (finished(group)) {
                doFinish(group.getGroupId());
            }
        }
        return 1;
    }

    private void doFinish(String groupId) {
        String gs = srt.opsForList().rightPop(SUCCESS_GROUP + groupId);
        while (StringUtils.hasText(gs)) {
            Group g = parseJson(gs, Group.class);
            if (Objects.equals(true, srt.hasKey(FAIL_STATUS + groupId + ":" + Status.TRYING_FAIL.ordinal()))) {
                g.setStatus(Status.CANCEL.ordinal());
                srt.opsForList().leftPush(CANCEL_GROUP + groupId, gs);
            } else {
                g.setStatus(Status.CONFIRM.ordinal());
                srt.opsForList().leftPush(CONFIRM_GROUP + groupId, gs);
            }
            if (setLock(g.getGroupId(), g.getXid(), g.getStatus())) {
                send(g);
            }
            gs = srt.opsForList().rightPop(SUCCESS_GROUP + groupId);
        }
        clean(groupId);
    }

    private void clean(String groupId) {
        srt.delete(START_GROUP + groupId);
        srt.delete(FAIL_STATUS + groupId);
        srt.delete(SUCCESS_GROUP + groupId);
        srt.delete(FINISH_COUNT + groupId);
        srt.delete(CONFIRM_GROUP + groupId);
        srt.delete(CANCEL_GROUP + groupId);
    }

    private boolean finished(Group g) {
        String s = srt.opsForValue().get(FINISH_COUNT + g.getGroupId());
        Long size = srt.opsForList().size(START_GROUP + g.getGroupId());
        return Objects.equals(s, String.valueOf(size));
    }

}
