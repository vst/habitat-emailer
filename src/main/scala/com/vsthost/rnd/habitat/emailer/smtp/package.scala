package com.vsthost.rnd.habitat.emailer

import cats.effect.IO


/**
  * Provides a definitions for convenience.
  */
package object smtp {
  /**
    * Provides a default instance for `Emailer[IO]` for the given SMTP configuration.
    *
    * @param config SMTP configuration.
    * @return An instance of [[Emailer]] for the [[IO]] instance.
    */
  def io(config: SMTPConfig): SMTPEmailer[IO] = new SMTPEmailer[IO](config)
}
