package com.pirum.exercises.worker

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.concurrent.Promise
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

class WorkerPool(workers: Int)(implicit ec: ExecutionContext) {
  private val execService = Executors.newScheduledThreadPool(1)
  private val queue = collection.mutable.Queue.empty[Promise[Unit]]
  private val semaphore = new AtomicInteger(workers)

  def shutdownNow(): Unit = {
    val _ = execService.shutdownNow()
  }

  def run(task: Task): Future[Unit] = {
    val promise = Promise[Unit]
    val n = semaphore.get()
    if (n > 0) {
      if (semaphore.compareAndSet(n, n - 1)) {
        val f = task.run(execService)
        f.onComplete { _ =>
          semaphore.incrementAndGet()
          queue.dequeueFirst(_ => true).fold(())(_.success(()))
        }
        f
      } else run(task)
    } else {
      val promise = Promise[Unit]
      queue.enqueue(promise)
      promise.future.flatMap(_ => run(task))
    }
  }
}
