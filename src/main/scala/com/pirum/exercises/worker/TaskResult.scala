package com.pirum.exercises.worker

trait TaskResult

object TaskResult {

  case object Success extends TaskResult
  case object Failure extends TaskResult
  case object Timeout extends TaskResult
}
