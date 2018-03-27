package org.rustkeylock.callbacks

import com.typesafe.scalalogging.Logger
import org.astonbitecode.j4rs.api.invocation.NativeCallbackSupport
import org.slf4j.LoggerFactory

class LogCb {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val INFO = "INFO"
  private val WARN = "WARN"
  private val ERROR = "ERROR"

  def log(level: String, path: String, file: String, line: Int, message: String): Unit = {
    val prefix = s"rkl NATIVE: $path (line: $line) "
    level match {
      case INFO => logger.info(prefix + message)
      case WARN => logger.warn(prefix + message)
      case ERROR => logger.error(prefix + message)
      case _ => logger.debug(prefix + message)
    }
  }
}