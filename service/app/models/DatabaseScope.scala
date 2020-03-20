package models

sealed trait DatabaseScope

case object INFERRED extends DatabaseScope
case object RELEASED extends DatabaseScope
