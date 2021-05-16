package com.pirum.exercises.worker

import java.util.concurrent.{Executors, TimeUnit, TimeoutException}
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

object Main extends Program {

  def main(args: Array[String]): Unit = {
    //main call to program goes here, for example:
    //program(List(LambdaTask(() => println("hello"))), FiniteDuration.apply(2, TimeUnit.SECONDS), workers = 1)
  }

  def program(tasks: List[Task], timeout: FiniteDuration, workers: Int): Unit = {
    val results = runTasks(tasks, timeout, workers)
    val summary = ResultSummary(results)
    println(summary)
  }

  private[worker] def runTasks(tasks: List[Task],
                               timeout: FiniteDuration = FiniteDuration.apply(2, TimeUnit.SECONDS),
                               workers: Int): List[TaskResult] = {
    val futures = runTasksPar(tasks, workers)

    val awaitExecService = Executors.newSingleThreadExecutor()
    implicit val awaitExecContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(awaitExecService)
    Try(Await.ready(Future.sequence(futures), timeout)).recover{ case _: TimeoutException => () }

    futures.map(_.value match {
      case None => TaskResult.Timeout
      case Some(Failure(ex)) =>
        println(s"[ERROR] Unexpected failure when measuring duration of the task ${ex.getMessage}")
        TaskResult.Failure(Int.MaxValue)
      case Some(Success((dur, Success(_)))) => TaskResult.Success(dur)
      case Some(Success((dur, Failure(_)))) => TaskResult.Failure(dur)
    })
  }

  private def runTasksPar(tasks: List[Task], workers: Int): List[Future[(Long, Try[Unit])]] = {
    if (tasks.isEmpty) Nil
    else {
      val execService = Executors.newFixedThreadPool(workers)
      val execContext = ExecutionContext.fromExecutorService(execService)

      tasks.map(t => Future(timed(t.run()))(execContext))
    }
  }
  private def timed(fn: => Unit): (Long, Try[Unit]) = {
    val t1 = System.currentTimeMillis()
    val res = Try(fn)
    val t2 = System.currentTimeMillis()
    (t2 - t1, res)
  }
}
