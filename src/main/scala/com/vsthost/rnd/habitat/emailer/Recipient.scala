package com.vsthost.rnd.habitat.emailer


/**
  * Defines a recipient encoding.
  *
  * @param rtype    Type of the recipient.
  * @param address  Address of the recipient.
  */
case class Recipient (rtype: RecipientType, address: Address)
