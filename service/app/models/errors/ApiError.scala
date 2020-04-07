package models.errors

import akka.util.ByteString
import play.api.http.HttpEntity
import play.api.mvc.{ResponseHeader, Result}

case class ApiError(status: Int, message: String) {

  def toResults() = {
    Result(ResponseHeader(status), HttpEntity.Strict(ByteString(message), None))
  }
}
