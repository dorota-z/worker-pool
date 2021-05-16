package com.pirum.exercises.worker

import org.junit.jupiter.api.Test
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.atomic.AtomicInteger
import scala.util.Success

object MainSpec extends Matchers {

  @Test
  def testRunZeroTasks(): Unit = {
    Main.runTasks(Nil) shouldBe Nil
  }

  @Test
  def testSingleSuccessfulResult(): Unit = {
    //given successful task
    val task = LambdaTask(() => ())
    //then run tasks should return success
    Main.runTasks(List(task)) shouldBe List(Success(()))
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
