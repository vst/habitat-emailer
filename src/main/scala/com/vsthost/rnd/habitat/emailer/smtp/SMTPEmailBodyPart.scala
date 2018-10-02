package com.vsthost.rnd.habitat.emailer.smtp

import java.nio.file.Path

/**
  * Defines a sealed trait for SMTP email body parts of interest.
  */
sealed trait SMTPEmailBodyPart

/**
  * Defines a plain text body part.
  *
  * @param text Plain text content.
  */
case class TextBodyPart (text: String) extends SMTPEmailBodyPart

/**
  * Defines an HTML body part.
  *
  * @param html HTML content.
  */
case class HtmlBodyPart (html: String) extends SMTPEmailBodyPart

/**
  * Defines a body part for an arbitrary, existing file path.
  *
  * @param path Path to the file.
  */
case class FileBodyPart (path: Path) extends SMTPEmailBodyPart
