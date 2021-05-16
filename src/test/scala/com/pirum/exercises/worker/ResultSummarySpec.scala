package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers

class ResultSummarySpec extends Matchers{

  @Test
  def testOnlySuccessfulResults(): Unit = {
    val summary = ResultSummary(List(
      TaskResult.Success(300),
      TaskResult.Success(400),
      TaskResult.Success(100)))
    summary.successful shouldBe List(3, 1, 2)
    summary.failed shouldBe Nil
    summary.timedOut shouldBe Nil
  }

  @Test
  def testMixedResults(): Unit = {
    val summary = ResultSummary(List(
      TaskResult.Failure(300),
      TaskResult.Failure(100),
      TaskResult.Success(200),
      TaskResult.Timeout))
    summary.successful shouldBe List(3)
    summary.failed shouldBe List(2, 1)
    summary.timedOut shouldBe List(4)
  }

  @Test
  def testReport(): Unit = {
    val summary = ResultSummary(List(
      TaskResult.Failure(300),
      TaskResult.Success(200),
      TaskResult.Timeout,
      TaskResult.Success(100)))
    summary.toString shouldBe
      """
        |Successful tasks: [Task4, Task2]
        |Failed tasks: [Task1]
        |Timed out tasks: [Task3]
        |""".stripMargin
  }
}
