package models

import common.models.NexusInstance
import org.scalatestplus.play.PlaySpec

class NexusInstanceSpec  extends PlaySpec {

  "GetIdFromUrl" should {
    "return the correct id from a complete Url" in {
      val id = "org/domain/schema/v0.0.1/qwe-ewq-ewq-w"
      val url = s"http://neuxs.humanbrainproject.org/v0/data/$id"
      val res = NexusInstance.getIdfromURL(url)
      res mustBe id
    }
    "return the id if it fit the criteria Url" in {
      val id = "org/domain/schema/v0.0.1/qwe-ewq-ewq-w"
      val res = NexusInstance.getIdfromURL(id)
      res mustBe id
    }
  }

}
