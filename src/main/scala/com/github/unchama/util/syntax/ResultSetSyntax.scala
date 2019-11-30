package com.github.unchama.util.syntax

import java.sql.ResultSet

import com.github.unchama.util.syntax

import scala.language.implicitConversions

trait ResultSetSyntax {
  implicit def toResultSetOps(resultSet: ResultSet): ResultSetSyntax.ResultSetOps =
    new syntax.ResultSetSyntax.ResultSetOps(resultSet)
}

object ResultSetSyntax {

  implicit class ResultSetOps(resultSet: ResultSet) {
    def recordIteration[T](operation: ResultSet => T): Option[T] = {
      try {
        var result: Option[T] = None
        while (resultSet.next()) {
          result = Some(operation(resultSet))
        }
        result
      } finally {
        resultSet.close()
      }
    }
  }

}
