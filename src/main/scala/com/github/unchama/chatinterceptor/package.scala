package com.github.unchama

import java.util.UUID

package object chatinterceptor {
  type ChatInterceptionScope = InterceptionScope[UUID, String]
  type InterceptionResult[R] = Either[R, CancellationReason]
}
