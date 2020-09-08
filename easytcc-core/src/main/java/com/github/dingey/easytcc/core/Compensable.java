package com.github.dingey.easytcc.core;

import java.lang.annotation.*;

/**
 * 补偿
 *
 * @author d
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@SuppressWarnings("unused")
public @interface Compensable {
    /**
     * 确认方法
     *
     * @return 方法名
     */
    String confirm() default "";

    /**
     * 取消方法
     *
     * @return 方法名
     */
    String cancel() default "";
}
