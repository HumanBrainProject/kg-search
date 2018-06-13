package controllers

import helpers.IDMHelper
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{EmptyBody, WSClient, WSResponse}
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KGController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext, ws: WSClient, config: Configuration)
  extends AbstractController(cc) {
  val logger = Logger(this.getClass)
  val apiEndpoint = config.get[String]("idm.api")
  val nexusEndpoint = config.get[String]("nexus.endpoint")
  val iamEndpoint = config.get[String]("nexus.iam")
  val orgNamePattern = "[a-z0-9]{3,}"

  def createPrivateSpace(): Action[AnyContent] = Action.async { implicit request =>
    val tokenOpt = request.headers.toSimpleMap.get("Authorization")
    tokenOpt match {
      case Some(token) =>
      request.body.asJson.map { jsonBody =>
        val groupName: String = (jsonBody \ "name").as[String].toLowerCase
        val isValidOrgName: Boolean = groupName.matches(orgNamePattern)
        if (isValidOrgName) {
          val description: String = (jsonBody \ "description").as[String]
          val nexusGroupName = s"nexus-$groupName"
          val adminGroupName = nexusGroupName + "-admin"
          for {
            nexusGroups <- KGController.createGroups(nexusGroupName, adminGroupName, description, token, apiEndpoint)
            nexusOrg <- KGController.createNexusOrg(groupName, token, nexusEndpoint)
            iamRights <- KGController.grantIAMrights(groupName, token, iamEndpoint)
          } yield {
            val res = List(s"OIDC group creation result: ${nexusGroups.statusText}\t content: ${nexusGroups.body}",
              s"Nexus organization creation result: ${nexusOrg.statusText}\t content: ${nexusOrg.body}",
              s"ACLs creation result: ${iamRights.statusText}\t content: ${iamRights.body}"
            )
            Ok(s"${res.mkString("\n")}")
          }
        } else {
          Future.successful(BadRequest("Invalid group name for nexus organization"))
        }
      }.getOrElse(Future.successful(BadRequest("Empty body")))
      case _ => Future.successful(Unauthorized)
    }
  }


}

object KGController {
  val logger = Logger(this.getClass)

  def createGroups(nexusGroupName: String, adminGroupName: String, groupDescription: String, token: String, endpoint: String)
                  (implicit ec: ExecutionContext, wSClient: WSClient): Future[WSResponse] = {
    val payload = createGroupPayload(nexusGroupName, groupDescription)
    val adminPayload = createGroupPayload(adminGroupName, groupDescription)
    val response = for {
      readGroup <- wSClient.url(endpoint + s"/group").addHttpHeaders("Authorization" -> token).post(payload)
      adminGroup <- wSClient.url(endpoint + s"/group").addHttpHeaders("Authorization" -> token).post(adminPayload)
    } yield (readGroup, adminGroup)

    response.flatMap { case (readGroup, adminGroup) =>
      (readGroup.status, adminGroup.status) match {
        case (201, 201) =>
          logger.debug(s"Groups created: $nexusGroupName, $adminGroupName")
          grantAdminToGroup(nexusGroupName, adminGroupName, token, endpoint)
        case (201, _) => Future.successful(adminGroup)
        case (_, 201) => Future.successful(readGroup)
        case (_, _) =>
          logger.error(s"Could not create group: \n group response :${readGroup.statusText} ${readGroup.body} \n admin group response : ${adminGroup.statusText}${adminGroup.body}")
          Future.successful(readGroup)
      }
    }
  }

  private def grantAdminToGroup(nexusGroupName: String, adminGroupName: String, token: String, endpoint: String)
                               (implicit ec: ExecutionContext, wSClient: WSClient): Future[WSResponse] = {
    val adminEnpoint = endpoint + s"/group/$adminGroupName/admin-groups/${IDMHelper.nexusAdmin}"
    val groupEnpoint = endpoint + s"/group/$nexusGroupName/admin-groups/$adminGroupName"

    val response = for {
      readGroup <- wSClient.url(groupEnpoint).addHttpHeaders("Authorization" -> token, "Content-length" -> "0").post(EmptyBody)
      adminGroup <- wSClient.url(adminEnpoint).addHttpHeaders("Authorization" -> token, "Content-length" -> "0").post(EmptyBody)
    } yield (readGroup, adminGroup)

    response.map { case (readGroup, adminGroup) =>
      (readGroup.status, adminGroup.status) match {
        case (201, 201) => readGroup
        case (201, _) => adminGroup
        case (_, 201) => readGroup
        case (_, _) =>
          logger.error(s"Could not assign group as admin: \n group response :${readGroup.body} \n admin group response : ${adminGroup.body}")
          readGroup

      }
    }
  }

  private def createGroupPayload(nexusGroupName: String, groupDescription: String): JsObject = {
    Json.obj(
      "description" -> groupDescription,
      "name" -> nexusGroupName
    )
  }

  def createNexusOrg(groupName: String, token: String, nexusEndpoint: String)
                    (implicit ec: ExecutionContext, wSClient: WSClient): Future[WSResponse] = {
    val payload = Json.obj(
      "@context" -> Json.obj(
        "schema" -> "http://schema.org"
      ),
      "schema:name" -> groupName
    )
    wSClient.url(nexusEndpoint + s"/v0/organizations/$groupName")
      .addHttpHeaders("Authorization" -> token)
      .put(payload)
  }

  def grantIAMrights(groupName: String, token: String, iamEndpoint: String)(implicit wSClient: WSClient): Future[WSResponse] = {
    val endpoint = iamEndpoint + s"/v0/acls/kg/$groupName"
    val payload = aclPayload(groupName)
    wSClient.url(endpoint).addHttpHeaders("Authorization" -> token).put(payload)
  }

  private def aclPayload(groupName: String): JsObject = {
    Json.obj(
      "acl" -> Json.arr(
        Json.obj(
          "identity" -> Json.obj(
            "realm" -> "HBP",
            "group" -> s"nexus-$groupName-admin",
            "@type" -> "GroupRef"
          ),
          "permissions" -> Json.arr(
            "read", "write", "own", "publish"
          )
        ),
        Json.obj(
          "identity" -> Json.obj(
            "realm" -> "HBP",
            "group" -> s"nexus-$groupName",
            "@type" -> "GroupRef"
          ),
          "permissions" -> Json.arr(
            "read"
          )
        )
      )
    )
  }

  def deleteOrg(groupName: String, token: String, nexusEndpoint: String)(implicit wSClient: WSClient): Future[WSResponse] = {
    wSClient
      .url(nexusEndpoint + s"/v0/organizations/$groupName?rev=1")
      .addHttpHeaders("Authorization" -> token)
      .delete()
  }

  def deleteGroup(groupName: String, token: String, apiEndpoint: String)(implicit wSClient: WSClient): Future[WSResponse] = {
    wSClient.url(apiEndpoint + s"/group/$groupName").addHttpHeaders("Authorization" -> token).delete()
  }

  def removeACLS(groupName: String, token: String, iamEndpoint: String)(implicit wSClient: WSClient): Future[WSResponse] = {
    wSClient.url(iamEndpoint + s"/v0/acls/kg/$groupName").addHttpHeaders("Authorization" -> token).delete()
  }


}
