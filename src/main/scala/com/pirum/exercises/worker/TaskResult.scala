package com.pirum.exercises.worker

trait TaskResult

object TaskResult {

  case class Success(duration: Long) extends TaskResult
  case class Failure(duration: Long) extends TaskResult
  case object Timeout extends TaskResult
}
