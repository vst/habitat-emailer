package com.vsthost.rnd.habitat.emailer

import cats.data.NonEmptyList
import cats.tagless.finalAlg

import scala.language.higherKinds


/**
  * Provides an email sending algebra.
  *
  * @tparam F Context type parameter.
  * @tparam A Type of the email content.
  */
@finalAlg
trait Emailer[F[_], A <: EmailBody] {
  /**
    * Defines a type alias for a non-empty list of recipients.
    *
    * We never want to work on an empty list of recipients.
    */
  type Recipients = NonEmptyList[Recipient]

  /**
    * Defines a type alias for send results.
    *
    * Essentially, it is [[Either]] a [[Left]] of [[Throwable]] in case of an error, or a [[Right]] of a [[String]]
    * identifier of the email sent.
    */
  type Result = Either[Throwable, String]

  /**
    * Attempts to send an email.
    *
    * @param sender       Sender of the email.
    * @param recipients   A non-empty list of recipients.
    * @param subject      The subject of the email.
    * @param content      The contents of the email.
    * @return A [[Result]] of email identifier if successful, or an error message otherwise.
    */
  def send(sender: Address, recipients: Recipients, subject: String, content: A): F[Result]
}
