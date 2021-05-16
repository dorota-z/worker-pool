package com.pirum.exercises.worker

import scala.util.Try

case class ResultSummary(taskResult: List[TaskResult]) {

  private[worker] def successful: List[Int] = taskResult.zipWithIndex
    .collect { case (TaskResult.Success(d), i) => (d, i + 1) }
    .sortBy(_._1)
    .map(_._2)
  private[worker] def failed: List[Int] = taskResult.zipWithIndex
    .collect { case (TaskResult.Failure(d), i) => (d, i + 1) }
    .sortBy(_._1)
    .map(_._2)
  private[worker] def timedOut: List[Int] = taskResult.zipWithIndex.collect { case (TaskResult.Timeout, i) => i + 1 }

  override def toString: String = {
    s"""
       |Successful tasks: [${successful.map(i => s"Task$i").mkString(", ")}]
       |Failed tasks: [${failed.map(i => s"Task$i").mkString(", ")}]
       |Timed out tasks: [${timedOut.map(i => s"Task$i").mkString(", ")}]
       |""".stripMargin
  }
}