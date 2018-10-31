package models

import models.instance.{EditorInstance, NexusInstance}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.Json

class EditorInstanceSpec extends PlaySpec{

  "CleanUp Reconciled fields" should {
    "remove the fields of the reconciled" in {
      val path = NexusPath("org", "domain", "schema", "version")
      val content = Json.obj(
        "http://hbp.eu/manual#test" -> "321",
        "http://hbp.eu/reconciled#test" -> "321",
        "@type" -> "http://minds#type",
        "http://schema.org/name" -> "test"
      )
      val nexusInstance = NexusInstance(
        None, path, content
      )
      val instance = EditorInstance(
        nexusInstance
      )

      val expected = EditorInstance(
        NexusInstance(
          None, path, Json.obj(
            "http://hbp.eu/manual#test" -> "321",
            "@type" -> "http://minds#type",
            "http://schema.org/name" -> "test"
          )
        )
      )

      expected mustBe instance.cleanReconciledFields()
    }
  }

}
