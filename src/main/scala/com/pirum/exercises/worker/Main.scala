package com.pirum.exercises.worker

import java.util.concurrent.{Executor, Executors, TimeUnit, TimeoutException}
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutorService, Future}
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

object Main extends App with Program {

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
      case Some(Failure(_)) => TaskResult.Failure
      case Some(Success(_)) => TaskResult.Success
    })
  }

  private def runTasksPar(tasks: List[Task], workers: Int): List[Future[Unit]] = {
    if (tasks.isEmpty) Nil
    else {
      val execService = Executors.newFixedThreadPool(workers)
      val execContext = ExecutionContext.fromExecutorService(execService)

      tasks.map(t => Future(t.run())(execContext))
    }
  }

  println("Good luck 🤓")
}
