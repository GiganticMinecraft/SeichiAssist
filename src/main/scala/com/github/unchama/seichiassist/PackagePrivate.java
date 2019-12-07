package com.github.unchama.seichiassist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * このアノテーションは、従来
 * <pre>
 *     // package-private
 * </pre>
 * などと書かれてきたコメントを置き換えるためのアノテーションです。
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
public @interface PackagePrivate {
}
