package models.excel_import

/*
 * This class represent a node in a graph of linked Entities, used 1 step insertion
 */
case class GraphNode (entity: Entity, children: Seq[GraphNode] = Seq.empty[GraphNode]){

  def addChild(newChild: GraphNode): GraphNode = {
    this.copy(children= children :+ newChild)
  }
}
