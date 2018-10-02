package helpers

import akka.util.ByteString
import common.helpers.ResponseHelper.{filterContentTypeAndLengthFromHeaders, flattenHeaders, getContentType}
import common.models.NexusPath
import editor.helpers.NavigationHelper
import play.api.http.HttpEntity
import play.api.libs.ws.WSResponse
import play.api.mvc.{ResponseHeader, Result}
import services.FormService

object ResponseHelper {
  /**
    * This function returns a Play Result with a back link
    * @param status The status of the response
    * @param headers The headers of the response
    * @param errorMsg The body of the response (could be either a String or JsValue)
    * @param originalPath The path of the instance
    * @param reconciledPrefix The reconciled prefix
    * @return A Play result with back link
    */
  def errorResultWithBackLink(status: Int, headers: Map[String, Seq[String]], errorMsg:Any, originalPath: NexusPath, reconciledPrefix: String, formService: FormService): Result = {
    val resultBackLink = NavigationHelper.errorMessageWithBackLink(errorMsg, NavigationHelper.generateBackLink(originalPath,reconciledPrefix, formService))
    Result(
      ResponseHeader(
        status,
        flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](headers))
      ),
      HttpEntity.Strict(ByteString(resultBackLink.toString()), Some("application/json"))
    )
  }

  /**
    * This function forward a reponse as a Play Result
    * @param res The response to be forwarded
    * @return A result reflecting the response
    */
  def forwardResultResponse(res: WSResponse): Result = {
    Result(
      ResponseHeader(res.status, flattenHeaders(filterContentTypeAndLengthFromHeaders[Seq[String]](res.headers))),
      HttpEntity.Strict(res.bodyAsBytes, getContentType(res.headers))
    )
  }
}
