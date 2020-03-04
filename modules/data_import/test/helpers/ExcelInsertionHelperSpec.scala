/*
 *   Copyright (c) 2018, EPFL/Human Brain Project PCO
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package helpers

import helpers.excel.ExcelInsertionHelper
import models.excel.{Entity, SingleValue}
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.collection.immutable.HashSet

class ExcelInsertionHelperSpec extends PlaySpec with GuiceOneAppPerSuite {

  "InsertionHelper" should {

    val usedEntities = HashSet.empty[String]
    val entitiesRef = Map(
      "S01" -> Entity(
        "Subject",
        "S01",
        Map(
          "species"       -> SingleValue("Homo sapiens "),
          "_is_member_of" -> SingleValue("MainSG "),
          "age category"  -> SingleValue("adult "),
          "age"           -> SingleValue("79 "),
          "sex"           -> SingleValue("female "),
          "alias"         -> SingleValue("jubrain-sub-01 "),
          "_ID"           -> SingleValue(" ")
        )
      ),
      "S02" -> Entity(
        "Subject",
        "S02",
        Map(
          "species"       -> SingleValue("Homo sapiens "),
          "_is_member_of" -> SingleValue("MainSG "),
          "age category"  -> SingleValue("adult "),
          "age"           -> SingleValue("55 "),
          "sex"           -> SingleValue("male "),
          "alias"         -> SingleValue("jubrain-sub-02"),
          "_ID"           -> SingleValue(" ")
        )
      ),
      "MainSG" -> Entity(
        "SubjectGroup",
        "MainSG",
        Map(
          "_ID"         -> SingleValue(" "),
          "alias"       -> SingleValue("MainSG "),
          "description" -> SingleValue(" ")
        )
      )
    )

    "build graph from seq of entities" in {
      val (rootRes, usedRes) =
        ExcelInsertionHelper.buildGraphFromEntity(entitiesRef.getOrElse("S01", null), usedEntities, entitiesRef)
      assert(rootRes.entity.localId == "S01")
      assert(rootRes.children.size == 1)
      assert(rootRes.children.head.entity.localId == "MainSG")
      assert(rootRes.children.head.children.isEmpty == true)
    }

    "build graph seq from all entities" in {
      val graphRoots = ExcelInsertionHelper.buildGraphsFromEntities(entitiesRef)

      assert(graphRoots.size == 2)

    }

    "build entities seq from a graph of entities" in {
      val graphRoots = ExcelInsertionHelper.buildGraphsFromEntities(entitiesRef)
      val entitiesList = ExcelInsertionHelper.buildEntitySeqFromGraph(graphRoots.head)

      assert(entitiesList.size == 2)
    }

    "build entities seq from entities ref" in {
      val entitiesList = ExcelInsertionHelper.buildInsertableEntitySeq(entitiesRef)

      assert(entitiesList.size == entitiesRef.size)

    }

  }

}
