package com.github.unchama.seichiassist

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import scala.annotation.Annotation


/**
 * このアノテーションは、従来
 * <pre>
 * // package-private
 * </pre>
 * などと書かれてきたコメントを置き換えるためのアノテーションです。
 */
@Retention(RetentionPolicy.SOURCE)
@Target(Array(ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD))
class PackagePrivate extends Annotation
