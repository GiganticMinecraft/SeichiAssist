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
    def recordIteration[T](operation: ResultSet => T): List[T] = {
      try {
        List.unfold(()) { _ =>
          if (resultSet.next())
            Some((operation(resultSet), ()))
          else
            None
        }
      } finally {
        resultSet.close()
      }
    }
  }

}
