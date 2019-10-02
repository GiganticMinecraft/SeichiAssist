package com.github.unchama.util.kotlin2scala;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE_USE })
public @interface SuspendingMethod { }
