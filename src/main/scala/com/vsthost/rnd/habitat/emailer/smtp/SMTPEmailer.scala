package com.vsthost.rnd.habitat.emailer.smtp

import java.util.Properties

import cats.effect._
import cats.implicits._
import com.vsthost.rnd.habitat.emailer.{Address, _}
import javax.activation.{DataHandler, FileDataSource}
import javax.mail._
import javax.mail.internet.{MimeBodyPart, MimeMessage, MimeMultipart}

import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

/**
  * Provides an SMTP emailing program for a [[Sync]] instance.
  *
  * @param config SMTP configuration.
  * @param F      Evidence for the context type parameter.
  * @tparam F     Context type parameter.
  */
class SMTPEmailer[F[_]](config: SMTPConfig)(implicit F : Sync[F]) extends Emailer[F, SMTPEmailBody] {
  /**
    * Defines the SMTP authenticator, if required.
    */
  private val authenticator: Option[Authenticator] = (config.username, config.password) match {
    case (Some(u), Some(p)) => Some(new Authenticator {
      override val getPasswordAuthentication = new PasswordAuthentication(u, p)
    })
    case _ => None
  }

  /**
    * Defines the SMTP session properties.
    */
  private val properties = {
    // Initialize the properties:
    val retval: Properties = new Properties

    // Populate properties:
    retval.put("mail.smtp.host", config.host)
    retval.put("mail.smtp.port", s"${config.port}")
    retval.put("mail.transport.protocol", "smtp")
    Some(config.tls).collect{ case true => retval.put("mail.smtp.starttls.enable", "true")}
    authenticator.foreach(_ => retval.put("mail.smtp.auth", "true"))

    // Done, return the populated properties:
    retval
  }

  /**
    * Defines the SMTP session.
    */
  private val session = Session.getInstance(properties, authenticator.orNull)

  /**
    * Creates and returns a new.
    */
  private def message: F[MimeMessage] =
    F.delay(new MimeMessage(session))


  /**
    * Returns the SMTP recipient type.
    *
    * @param rtype Our recipient type.
    * @return SMTP library recipient type.
    */
  private def getRecipientType(rtype: RecipientType): Message.RecipientType = rtype match {
    case TO => Message.RecipientType.TO
    case CC => Message.RecipientType.CC
    case BCC => Message.RecipientType.BCC
  }

  /**
    * Sets the message content.
    *
    * @param message  Message to send content of.
    * @param body     An instance of our SMTP email body specification.
    * @return Nothingness.
    */
  private def setContents(message: MimeMessage, body: SMTPEmailBody): Unit = body match {
    case EmptyEmailBody => message.setContent("", "text/plain; charset=utf-8")
    case MultiEmailBody(None, None, Nil) => message.setContent("", "text/plain; charset=utf-8")
    case MultiEmailBody(Some(text), None, Nil) => message.setContent(text, "text/plain; charset=utf-8")
    case MultiEmailBody(None, Some(html), Nil) => message.setContent(html, "text/html; charset=utf-8")
    case MultiEmailBody(None, None, parts) =>
      // Define the multi-part:
      val multipart = new MimeMultipart()

      // Prepend text/html parts (if any), and iterate over all parts to add:
      (TextBodyPart("") :: parts).foreach {
        case TextBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setText(str, "utf-8")
          multipart.addBodyPart(part)
        case HtmlBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setContent(str, "text/html; charset=utf-8")
          multipart.addBodyPart(part)
        case FileBodyPart(path) =>
          val part = new MimeBodyPart()
          val source = new FileDataSource(path.toFile)
          part.setDataHandler(new DataHandler(source))
          part.setFileName(path.toFile.getName)
          multipart.addBodyPart(part)
      }

      // Set the message content:
      message.setContent(multipart)
    case MultiEmailBody(Some(text), None, parts) =>
      // Define the multi-part:
      val multipart = new MimeMultipart()

      // Prepend text/html parts (if any), and iterate over all parts to add:
      (TextBodyPart(text) :: parts).foreach {
        case TextBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setText(str, "utf-8")
          multipart.addBodyPart(part)
        case HtmlBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setContent(str, "text/html; charset=utf-8")
          multipart.addBodyPart(part)
        case FileBodyPart(path) =>
          val part = new MimeBodyPart()
          val source = new FileDataSource(path.toFile)
          part.setDataHandler(new DataHandler(source))
          part.setFileName(path.toFile.getName)
          multipart.addBodyPart(part)
      }

      // Set the message content:
      message.setContent(multipart)
    case MultiEmailBody(None, Some(html), parts) =>
      // Define the multi-part:
      val multipart = new MimeMultipart()

      // Prepend text/html parts (if any), and iterate over all parts to add:
      (HtmlBodyPart(html) :: parts).foreach {
        case TextBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setText(str, "utf-8")
          multipart.addBodyPart(part)
        case HtmlBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setContent(str, "text/html; charset=utf-8")
          multipart.addBodyPart(part)
        case FileBodyPart(path) =>
          val part = new MimeBodyPart()
          val source = new FileDataSource(path.toFile)
          part.setDataHandler(new DataHandler(source))
          part.setFileName(path.toFile.getName)
          multipart.addBodyPart(part)
      }

      // Set the message content:
      message.setContent(multipart)
    case MultiEmailBody(Some(text), Some(html), parts) =>
      // Define the multi-part as related:
      val wrap = new MimeBodyPart()

      // Create the cover as "alternative":
      val cover = new MimeMultipart("alternative")

      // First, add the plain text part:
      val partText = new MimeBodyPart()
      partText.setText(text, "utf-8")
      cover.addBodyPart(partText)

      // Now, add the HTML part:
      val partHtml = new MimeBodyPart()
      partHtml.setContent(html, "text/html; charset=utf-8")
      cover.addBodyPart(partHtml)

      // Add the cover:
      wrap.setContent(cover)

      // Define the multi-part as "related:
      val multipart = new MimeMultipart("related")

      // Add the wrap:
      multipart.addBodyPart(wrap)

      // Iterate over all attachments and add:
      parts.foreach {
        case TextBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setText(str, "utf-8")
          multipart.addBodyPart(part)
        case HtmlBodyPart(str) =>
          val part = new MimeBodyPart()
          part.setContent(str, "text/html; charset=utf-8")
          multipart.addBodyPart(part)
        case FileBodyPart(path) =>
          val part = new MimeBodyPart()
          val source = new FileDataSource(path.toFile)
          part.setDataHandler(new DataHandler(source))
          part.setFileName(path.toFile.getName)
          multipart.addBodyPart(part)
      }

      // Set the message content:
      message.setContent(multipart)
  }

  /**
    * Attempts to send an email.
    *
    * @param sender     Sender of the email.
    * @param recipients A non-empty list of recipients.
    * @param subject    The subject of the email.
    * @param content    The contents of the email.
    * @return A [[Result]] of email identifier if successful, or an error message otherwise.
    */
  override def send(sender: Address, recipients: Recipients, subject: String, content: SMTPEmailBody): F[Result] = for {
    // Get the message:
    m <- message

    // Set the sender:
    _ = m.setFrom(sender.toInetAddress)

    // Add recipients:
    _ = recipients.map(r => m.addRecipient(getRecipientType(r.rtype), r.address.toInetAddress))

    // Set the subject:
    _ = m.setSubject(subject)

    // Set content(s):
    _ = setContents(m, content)

    // Attempt to send and return:
    r = Try(Transport.send(m)) match {
      case Failure(e) => Left(e)
      case Success(_) => Right(m.getMessageID)
    }
  } yield r
}
