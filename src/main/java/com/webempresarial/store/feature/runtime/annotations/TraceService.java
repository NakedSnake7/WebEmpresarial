package com.webempresarial.store.feature.runtime.annotations;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TraceService {

    String name() default "";

    String source() default "Application";
}