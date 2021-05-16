package com.pirum.exercises.worker

/***
 * Tasks represents a single job which can succeed, fail or hang
 * In this project the tasks are not expected to return result to the caller,
 * that is why the result of the run function is a Unit
 * Howev
 */
trait Task {
  def run(): Unit
}

case class LambdaTask(run: () => Unit) extends Task
