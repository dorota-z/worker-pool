package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicInteger
import scala.util.{Failure, Success}

object MainSpec extends Matchers {

  @Test
  def testRunZeroTasks(): Unit = {
    Main.runTasks(Nil) shouldBe Nil
  }

  @Test
  def testSingleSuccessfulTask(): Unit = {
    //given successful task
    val task = LambdaTask(() => ())
    //then run tasks should return success in the list
    Main.runTasks(List(task)) shouldBe List(Success(()))
  }

  @Test
  def testSingleFailingTask(): Unit = {
    val exception = new Exception("some ex")
    //given failing task
    val task = LambdaTask(() => throw exception)
    //then run tasks should return the failure in the list
    Main.runTasks(List(task)) shouldBe List(Failure(exception))
  }

  @Test
  def testMixedTasks(): Unit = {
    val exception = new Exception("some ex")
    //given failing task
    val failingTask = LambdaTask(() => throw exception)
    val successfulTask = LambdaTask(() => ())
    //then run tasks should return the failure in the list
    Main.runTasks(List(failingTask, successfulTask)) shouldBe List(Failure(exception), Success())
  }

  @Test
  def testTaskGetsCalledExactlyOnce(): Unit = {
    //given a side effecting task
    val i = new AtomicInteger(0)
    val incrementingTask = LambdaTask(() => i.incrementAndGet())
    //when task gets run
    Main.runTasks(List(incrementingTask))
    //then side effect should happen exactly once
    i.get shouldBe 1
  }
}
