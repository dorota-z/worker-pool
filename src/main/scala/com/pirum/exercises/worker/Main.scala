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
    val workerPool = new WorkerPool(workers)
    val futures = workerPool.runTasks(tasks)

    val awaitExecService = Executors.newSingleThreadExecutor()
    implicit val awaitExecContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(awaitExecService)
    Try(Await.ready(Future.sequence(futures), timeout)).recover{ case _: TimeoutException => () }

    val results = futures.map(_.value match {
      case None => TaskResult.Timeout
      case Some(Failure(ex)) =>
        println(s"[ERROR] Unexpected failure when measuring duration of the task ${ex.getMessage}")
        ex.printStackTrace()
        TaskResult.Failure(Int.MaxValue)
      case Some(Success((dur, Success(_)))) => TaskResult.Success(dur)
      case Some(Success((dur, Failure(_)))) => TaskResult.Failure(dur)
    })

    workerPool.shutdownNow()

    results
  }

}
