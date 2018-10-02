package com.vsthost.rnd.habitat.emailer


/**
  * Defines available recipient types.
  */
sealed trait RecipientType
case object TO extends RecipientType
case object CC extends RecipientType
case object BCC extends RecipientType
