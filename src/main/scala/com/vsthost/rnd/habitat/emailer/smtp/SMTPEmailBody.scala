package com.vsthost.rnd.habitat.emailer.smtp

import com.vsthost.rnd.habitat.emailer.EmailBody


/**
  * Defines a sealed trait for SMTP email body.
  */
sealed trait SMTPEmailBody extends EmailBody

/**
  * Defines an empty email body.
  */
case object EmptyEmailBody extends SMTPEmailBody

/**
  * Defines an email body with text and/or html parts and also additional SMTP body parts.
  *
  * @param text   Optional plain text content
  * @param html   Optioanl HTML content.
  * @param parts  Additional attachments.
  */
case class MultiEmailBody(text: Option[String],
                          html: Option[String],
                          parts: List[SMTPEmailBodyPart]) extends SMTPEmailBody
