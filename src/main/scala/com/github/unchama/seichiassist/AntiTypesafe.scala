package com.github.unchama.seichiassist

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import scala.annotation.Annotation


@Target(Array[ElementType](ElementType.METHOD, ElementType.FIELD))
@Retention(RetentionPolicy.SOURCE)
class AntiTypesafe extends Annotation