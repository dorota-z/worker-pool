package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

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
    Main.runTasks(List(task)) shouldBe List(TaskResult.Success)
  }

  @Test
  def testSingleFailingTask(): Unit = {
    val exception = new Exception("some ex")
    //given failing task
    val task = LambdaTask(() => throw exception)
    //then run tasks should return the failure in the list
    Main.runTasks(List(task)) shouldBe List(TaskResult.Failure)
  }

  @Test
  def testHangingTask(): Unit = {
    //given a hanging task
    val latch = new CountDownLatch(1)
    val task = LambdaTask(() => latch.await())
    //then run tasks should return the timeout failure in the list after timeout
    val timeout = Duration.apply(200, TimeUnit.MILLISECONDS)
    try {
      val run = Future(Main.runTasks(List(task), timeout))
      Await.result(run, timeout * 2) shouldBe List(TaskResult.Timeout)
    } finally {
      latch.countDown()
    }
  }

  @Test
  def testHangingTaskMixedWithSuccessful(): Unit = {
    //given a hanging task
    val latch = new CountDownLatch(1)
    val hangingTask = LambdaTask(() => latch.await())
    val successfulTask = LambdaTask(() => ())
    //then run tasks should return the timeout failure in the list after timeout
    val timeout = Duration.apply(200, TimeUnit.MILLISECONDS)
    try {
      val run = Future(Main.runTasks(List(hangingTask, successfulTask), timeout))
      Await.result(run, timeout * 2) shouldBe List(TaskResult.Timeout, TaskResult.Success)
    } finally {
      latch.countDown()
    }
  }

  @Test
  def testFailingTaskMixedWithSuccessful(): Unit = {
    val exception = new Exception("some ex")
    //given failing task
    val failingTask = LambdaTask(() => throw exception)
    val successfulTask = LambdaTask(() => ())
    //then run tasks should return the failure in the list
    Main.runTasks(List(failingTask, successfulTask)) shouldBe List(TaskResult.Failure, TaskResult.Success)
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
