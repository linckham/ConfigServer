package com.cmbc.configserver.remoting.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * the field can be null if using this annotation
 * @author tongchuan.lin<linckham@gmail.com>
 * @since 2014/10/17 3:01:22PM
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE })
public @interface CFNullable {
}
