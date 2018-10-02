package com.vsthost.rnd.habitat.emailer

import javax.mail.internet.InternetAddress

import scala.util.Try

/**
  * Defines an email address representation.
  *
  * @param value Email address value.
  */
case class Address(value: String, name: Option[String]) {
  /**
    * Converts the email address to [[InternetAddress]].
    *
    * @return An [[InternetAddress]] instance for the [[Address]].
    */
  def toInetAddress: InternetAddress = name match {
    case None => new InternetAddress(value)
    case Some(p) => new InternetAddress(value, p)
  }
}

/**
  * Provides a companion to [[Address]] encoding for convenience definitions and functions.
  */
object Address {
  /**
    * Attempts to build an [[Address]] instance from given value.
    *
    * @param value Value to build [[Address]] instance from.
    * @return [[Some]] [[Address]] if given value is conformant, [[None]] otherwise.
    */
  def apply(value: String): Option[Address] = Try({
    val inet = new InternetAddress(value, true)
    inet.validate()
    inet
  }).toOption.map(i => new Address(i.getAddress, Option(i.getPersonal)))
}
