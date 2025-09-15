package consumers.no_registral.sujeto.infrastructure

import akka.Done

package object http {
  case class StartReprocessing()
  case class StopReprocessing()
  case class ReprocessingCompleted(result: Done)
  case class ReprocessingFailed(error: Throwable)
}
