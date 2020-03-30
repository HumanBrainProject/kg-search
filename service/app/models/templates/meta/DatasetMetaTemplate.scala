package models.templates.meta

import models.templates.Template
import utils._

trait DatasetMetaTemplate extends Template {

  val template = Map(
    "identifier" -> ObjectValue(
      List(CustomField[Boolean]("searchUi:ignoreForSearch", "https://schema.hbp.eu/searchUi/ignoreForSearch"))
    )
  )
}
