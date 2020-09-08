package com.github.dingey.easytcc.core;

public enum Status {
    READY, TRYING_START, TRYING_SUCCESS, TRYING_FAIL, CONFIRM, CANCEL;

    public static String name(int ordinal) {
        for (Status s : Status.values()) {
            if (s.ordinal() == ordinal) {
                return s.name();
            }
        }
        return Status.READY.name();
    }
}
