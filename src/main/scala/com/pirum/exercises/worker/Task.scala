package com.pirum.exercises.worker

/***
 * Tasks represents a single job which can succeed, fail or hang
 */
trait Task {
  def run: () => Unit
}

case class LambdaTask(run: () => Unit) extends Task
