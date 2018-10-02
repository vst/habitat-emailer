package com.vsthost.rnd.habitat.emailer.smtp

/**
  * Defines the SMTP configuratiojn.
  *
  * @param host SMTP server host.
  * @param port SMTP server port.
  * @param tls Start TLS?
  * @param username Optional username for authenticating to SMTP server.
  * @param password Optional password for authenticating to SMTP server.
  */
case class SMTPConfig(host: String, port: Int, tls: Boolean, username: Option[String], password: Option[String])
