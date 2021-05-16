package com.pirum.exercises.worker

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WorkerPool(workers: Int) {
  private val execService = Executors.newFixedThreadPool(workers)
  private val execContext = ExecutionContext.fromExecutorService(execService)

  def shutdownNow(): Unit = {
    execContext.shutdownNow()
  }

  def runTasks(tasks: List[Task]): List[Future[(Long, Try[Unit])]] = {
    if (tasks.isEmpty) Nil
    else tasks.map(t => Future(timed(t.run()))(execContext))
  }

  def timed(fn: => Unit): (Long, Try[Unit]) = {
    val t1 = System.currentTimeMillis()
    val res = Try(fn)
    val t2 = System.currentTimeMillis()
    (t2 - t1, res)
  }
}
