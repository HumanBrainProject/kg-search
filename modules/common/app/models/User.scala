package common.models

trait User{
  val id: String
  val email: String
  val groups: Seq[String]
  val name:String
}