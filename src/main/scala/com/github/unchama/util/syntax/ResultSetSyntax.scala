package com.github.unchama.util.syntax

import java.sql.ResultSet

import com.github.unchama.util.syntax

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
      } catch {
        case e: Exception => e.printStackTrace(); None
      } finally {
        resultSet.close()
      }
    }
  }

}
