package com.pirum.exercises.worker

import scala.util.Try

case class ResultSummary(successful: List[Int], failed: List[Int]) {

  override def toString: String = {
    s"""
       |Successful tasks: [${successful.map(i => s"Task$i").mkString(", ")}]
       |Failed tasks: [${failed.map(i => s"Task$i").mkString(", ")}]
       |""".stripMargin
  }
}

object ResultSummary {

  def apply(results: List[Try[Unit]]): ResultSummary = {
    val withIndex = results.zipWithIndex.map { case (res, idx) => (res, idx + 1) }
    val (successful, failed) = withIndex.partition(_._1.isSuccess)
    val successfulIndices = successful.map(_._2)
    val failedIndices = failed.map(_._2)
    ResultSummary(successful = successfulIndices, failed = failedIndices)
  }
}