package org.apache.ignite.internal.processors.query.h2.opt;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface DocScore {
}
