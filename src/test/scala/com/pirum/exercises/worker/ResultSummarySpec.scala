package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success}

class ResultSummarySpec extends Matchers{

  @Test
  def testOnlySuccessfulResults(): Unit = {
    ResultSummary(List(Success(), Success())) shouldBe ResultSummary(List(1, 2), Nil)
  }

  @Test
  def testMixedResults(): Unit = {
    ResultSummary(List(Failure(new Exception("some ex")), Success())) shouldBe ResultSummary(List(2), List(1))
  }

  @Test
  def testReport(): Unit = {
    ResultSummary(List(2, 5), List(1, 3, 4)).toString shouldBe
      """
        |Successful tasks: [Task2, Task5]
        |Failed tasks: [Task1, Task3, Task4]
        |""".stripMargin
  }
}
