package dataimport.helpers.excel_import

import models.excel_import.{Entity, GraphNode}
import play.api.Logger

import scala.collection.immutable.HashSet


object InsertionHelper {

  val logger = Logger(this.getClass)

  def buildInsertableEntitySeq(entitiesRef: Map[String, Entity]) = {
    val graphRoots = buildGraphsFromEntities(entitiesRef)
    graphRoots.flatMap(buildEntitySeqFromGraph)
  }

  def buildEntitySeqFromGraph(root: GraphNode): Seq[Entity] = {
    // DFS
    if (root.children.isEmpty){
      Seq(root.entity)
    } else {
      root.children.foldLeft(Seq.empty[Entity]){
        case (entities, child) =>
          entities ++ buildEntitySeqFromGraph(child)
      } :+ root.entity
    }
  }

  def buildGraphsFromEntities(entitiesRef: Map[String, Entity]): Seq[GraphNode] =  {
    entitiesRef.map(_._2).foldLeft((Seq.empty[GraphNode], HashSet.empty[String])){
      case ((graphRoots, usedEntities), entity) =>
        if (usedEntities.contains(entity.id)) {
          (graphRoots, usedEntities) // this entity is used already
        } else {
          val (graphRoot, usedEntititesUpdated) = buildGraphFromEntity(entity, usedEntities, entitiesRef)
          (graphRoots :+ graphRoot, usedEntititesUpdated)
        }
    }._1
  }


  def buildGraphFromEntity(entity: Entity, initialUsedEntities: HashSet[String], entitiesRef: Map[String, Entity] ): (GraphNode, HashSet[String]) = {
    val usedEntities = initialUsedEntities + entity.id
    val entityLinks = entity.getLinkedIds().filter(e => !usedEntities.contains(e)) // filter out already used one

    val (rootRes, usedEntitiesRes) = entityLinks.foldLeft((GraphNode(entity), usedEntities)){
      case ((root, usedEnt), id) =>
        entitiesRef.get(id) match {
          case Some(foundEntity) =>
            val (child, newUsedEnt) = buildGraphFromEntity(foundEntity, usedEnt, entitiesRef)
            (root.addChild(child), newUsedEnt)
          case None => logger.error(s"${root.entity.`type`}: ${root.entity.id} - reference to unknown data: $id")
            (root, usedEnt)
        }
    }
    (rootRes, usedEntitiesRes)
  }



}
