package org.rustkeylock.callbacks

import org.rustkeylock.api.InterfaceWithRust
import org.rustkeylock.api.LoggingCallback
import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger

class LogCb extends LoggingCallback {
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  private val INFO = "INFO"
	private val WARN = "WARN"
	private val ERROR = "ERROR"

  override def apply(level: String, path: String, file: String, line: Int, message: String): Unit = {
    if (!path.startsWith("hyper") && !path.startsWith("tokio")) {
      val prefix = s"rkl NATIVE: $path (line: $line) "
      level match {
        case INFO => logger.info(prefix + message)
        case WARN => logger.warn(prefix + message)
        case ERROR => logger.error(prefix + message)
        case _ => logger.debug(prefix + message)
      }
    }
  }
}