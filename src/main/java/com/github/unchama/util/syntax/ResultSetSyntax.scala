package com.github.unchama.util.syntax

import java.sql.ResultSet

object ResultSetSyntax {
  implicit class ResultSetOps(val resultSet: ResultSet) {
    def recordIteration[T](operation: ResultSet => T): Option[T] = {
      try {
        var result: Option[T] = None
        while (resultSet.next()) {
          result = Some(operation())
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
