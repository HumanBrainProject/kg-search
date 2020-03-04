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
package helpers.excel

import models.excel.GraphNode
import models.excel.{Entity, GraphNode}
import play.api.Logger

import scala.collection.immutable.HashSet

object ExcelInsertionHelper {

  val logger = Logger(this.getClass)

  def buildInsertableEntitySeq(entitiesRef: Map[String, Entity]): Seq[Entity] = {
    val graphRoots = buildGraphsFromEntities(entitiesRef)
    graphRoots.flatMap(buildEntitySeqFromGraph)
  }

  def buildEntitySeqFromGraph(root: GraphNode): Seq[Entity] = {
    // DFS
    if (root.children.isEmpty) {
      Seq(root.entity)
    } else {
      root.children.foldLeft(Seq.empty[Entity]) {
        case (entities, child) =>
          entities ++ buildEntitySeqFromGraph(child)
      } :+ root.entity
    }
  }

  def buildGraphsFromEntities(entitiesRef: Map[String, Entity]): Seq[GraphNode] = {
    entitiesRef
      .map(_._2)
      .foldLeft((Seq.empty[GraphNode], HashSet.empty[String])) {
        case ((graphRoots, usedEntities), entity) =>
          if (usedEntities.contains(entity.localId)) {
            (graphRoots, usedEntities) // this entity is used already
          } else {
            val (graphRoot, usedEntititesUpdated) = buildGraphFromEntity(entity, usedEntities, entitiesRef)
            (graphRoots :+ graphRoot, usedEntititesUpdated)
          }
      }
      ._1
  }

  def buildGraphFromEntity(
    entity: Entity,
    initialUsedEntities: HashSet[String],
    entitiesRef: Map[String, Entity]
  ): (GraphNode, HashSet[String]) = {
    val usedEntities = initialUsedEntities + entity.localId
    val entityLinks = entity.getInternalLinkedIds().filter(e => !usedEntities.contains(e)) // filter out already used one

    val (rootRes, usedEntitiesRes) = entityLinks.foldLeft((GraphNode(entity), usedEntities)) {
      case ((root, usedEnt), id) =>
        entitiesRef.get(id) match {
          case Some(foundEntity) =>
            val (child, newUsedEnt) = buildGraphFromEntity(foundEntity, usedEnt, entitiesRef)
            (root.addChild(child), newUsedEnt)
          case None =>
            logger.info(s"${root.entity.`type`}: ${root.entity.localId} - reference to unknown data: $id")
            (root, usedEnt)
        }
    }
    (rootRes, usedEntitiesRes)
  }
}
