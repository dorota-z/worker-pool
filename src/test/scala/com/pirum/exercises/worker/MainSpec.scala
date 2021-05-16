package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.concurrent.Eventually.eventually
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Span}

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{CountDownLatch, TimeUnit}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, Future}

object MainSpec extends Matchers {

  @Test
  def testRunZeroTasks(): Unit = {
    Main.runTasks(Nil, workers = 1) shouldBe Nil
  }

  @Test
  def testSingleSuccessfulTask(): Unit = {
    //given successful task
    val task = LambdaTask(() => ())
    //then run tasks should return success in the list
    Main.runTasks(List(task), workers = 1) match {
      case  List(TaskResult.Success(_)) =>
      case  _ => fail()
    }
  }

  @Test
  def testSingleFailingTask(): Unit = {
    val exception = new Exception("some ex")
    //given failing task
    val task = LambdaTask(() => throw exception)
    //then run tasks should return the failure in the list
    Main.runTasks(List(task), workers = 1) match {
      case  List(TaskResult.Failure(_)) =>
      case  _ => fail()
    }
  }

  @Test
  def testSingleHangingTask(): Unit = {
    //given a hanging task
    val mutex = "mutex"
    val latch = new CountDownLatch(1)
    val task = LambdaTask(() => mutex.synchronized { latch.await() })
    //then run tasks should return the timeout failure in the list after timeout
    val timeout = Duration.apply(200, TimeUnit.MILLISECONDS)
    try {
      val run = Future {
        val res = Main.runTasks(List(task), timeout, workers = 1)
        mutex.synchronized {
          "mutex no longer held by the task, the task should have been stopped"
        }
        res
      }
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
      val run = Future(Main.runTasks(List(hangingTask, successfulTask), timeout, workers = 2))
      Await.result(run, timeout * 2) match {
        case  List(TaskResult.Timeout, TaskResult.Success(_)) =>
        case  _ => fail()
      }
    } finally {
      latch.countDown()
    }
  }

  @Test
  def testLongRunningTaskWithQuickOne(): Unit = {
    //given a hanging task
    val slowTask = LambdaTask(() => Thread.sleep(200))
    val quickTask = LambdaTask(() => ())
    //then run tasks should return successful result for both
    val timeout = Duration.apply(500, TimeUnit.MILLISECONDS)
    val run = Future(Main.runTasks(List(slowTask, quickTask), timeout, workers = 2))
    Await.result(run, timeout * 2) match {
      case  List(TaskResult.Success(longDuration), TaskResult.Success(shortDuration)) =>
        longDuration should be > 200L
        shortDuration should be < 100L
      case  _ => fail()
    }
  }

  @Test
  def testFailingTaskMixedWithSuccessful(): Unit = {
    val exception = new Exception("some ex")
    //given failing task
    val failingTask = LambdaTask(() => throw exception)
    val successfulTask = LambdaTask(() => ())
    //then run tasks should return the failure in the list
    Main.runTasks(List(failingTask, successfulTask), workers = 2)  match {
      case  List(TaskResult.Failure(_), TaskResult.Success(_)) =>
      case  _ => fail()
    }
  }

  @Test
  def testWorkerNumbersNotExceeded(): Unit = {
    val started = (0 to 2).map(_ => new CountDownLatch(1))
    val allowedToFinish = (0 to 2).map(_ => new CountDownLatch(1))

    try {
      //given tasks that block on a latch
      val tasks = (0 to 2).map(i => LambdaTask(() => {
        started(i).countDown()
        println(s"started $i")
        allowedToFinish(i).await()
        println(s"finishing $i")
      }))

      //when tasks are run using fewer workers
      val workers = 2
      Future {
        Main.runTasks(tasks.toList, timeout = FiniteDuration.apply(3, TimeUnit.SECONDS), workers = workers)
        println("finished whole run")
      }

      //then he count of tasks that have started should be equal to workers
      eventually(timeout = Timeout(Span(200, Millis))) {
        started.count(_.getCount == 0) shouldBe workers
      }
      Thread.sleep(200)
      //and the count of tasks that have started should remain equal to workers
      started.count(_.getCount == 0) shouldBe workers

      //when the started tasks are allowed to finish
      allowedToFinish.foreach(_.countDown())
      //then he count of tasks that have started should increase
      eventually(timeout = Timeout(Span(500, Millis))) {
        started.count(_.getCount == 0) shouldBe tasks.length
      }
    } finally {
      allowedToFinish.foreach(_.countDown())
    }
  }

  @Test
  def testTaskGetsCalledExactlyOnce(): Unit = {
    //given a side effecting task
    val i = new AtomicInteger(0)
    val incrementingTask = LambdaTask(() => i.incrementAndGet())
    //when task gets run
    Main.runTasks(List(incrementingTask), workers = 1)
    //then side effect should happen exactly once
    i.get shouldBe 1
  }
}
