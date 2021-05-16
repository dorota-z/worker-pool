package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers

class ResultSummarySpec extends Matchers{

  @Test
  def testOnlySuccessfulResults(): Unit = {
    val summary = ResultSummary(List(TaskResult.Success, TaskResult.Success))
    summary.successful shouldBe List(1, 2)
    summary.failed shouldBe Nil
    summary.timedOut shouldBe Nil
  }

  @Test
  def testMixedResults(): Unit = {
    val summary = ResultSummary(List(TaskResult.Failure, TaskResult.Success, TaskResult.Timeout))
    summary.successful shouldBe List(2)
    summary.failed shouldBe List(1)
    summary.timedOut shouldBe List(3)
  }

  @Test
  def testReport(): Unit = {
    val summary = ResultSummary(List(TaskResult.Failure, TaskResult.Success, TaskResult.Timeout, TaskResult.Success))
    summary.toString shouldBe
      """
        |Successful tasks: [Task2, Task4]
        |Failed tasks: [Task1]
        |Timed out tasks: [Task3]
        |""".stripMargin
  }
}
