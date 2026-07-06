package com.webempresarial.store.feature.runtime.annotations;

import com.webempresarial.store.feature.runtime.TraceType;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Trace {

    TraceType type();

    String name() default "";

    String source() default "Application";

    boolean rootIfMissing() default false;
}