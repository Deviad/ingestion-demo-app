package com.foobar.foobarchallenge.common;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import org.springframework.stereotype.Component;

@Component
@Retention(RUNTIME)
public @interface Adapter {}
