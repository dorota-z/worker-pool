package com.pirum.exercises.worker

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

object Main extends App with Program {

  def program(tasks: List[Task], timeout: FiniteDuration, workers: Int): Unit = {
    val results = runTasks(tasks)
    val summary = ResultSummary(results)
    println(summary)
  }

  private[worker] def runTasks(tasks: List[Task]): List[Try[Unit]] = {
    tasks.map(t => Try(t.run()))
  }

  println("Good luck 🤓")
}
