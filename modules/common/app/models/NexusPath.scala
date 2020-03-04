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

package models

final case class NexusPath(org: String, domain: String, schema: String, version: String) {

  override def toString(): String = {
    Seq(org, domain, schema, version).mkString("/")
  }

  def withSpecificSubspace(subspace: String): NexusPath = {
    this.copy(org = org + subspace)
  }

  /**
    * This method returns the path of the instance with the original organization instead of the reconciled space
    * @param reconciledSuffix The term used to identify the reconciled space
    * @return A NexusPath object with an original (non reconciled) organization
    */
  def originalPath(reconciledSuffix: String): NexusPath = {
    val org = if (this.isReconciled(reconciledSuffix)) {
      val editorReconciled = s"""^(.+)$reconciledSuffix$$""".r
      this.org match {
        case editorReconciled(originalOrg) => originalOrg
      }
    } else {
      this.org
    }
    NexusPath(org, this.domain, this.schema, this.version)
  }

  /**
    * This method return a path with a reconciled organization
    * @param reconciledSuffix The term used to identify the reconciled space
    * @return A NexusPath object with an reconciled organization
    */
  def reconciledPath(reconciledSuffix: String): NexusPath = {
    assert(!this.org.endsWith(reconciledSuffix))
    NexusPath(NexusPath.addSuffixToOrg(this.org, reconciledSuffix), this.domain, this.schema, this.version)
  }

  def isReconciled(reconciledSuffix: String): Boolean = {
    this.org.endsWith(reconciledSuffix)
  }
}

object NexusPath {

  def apply(args: Seq[String]): NexusPath = {
    NexusPath(args(0), args(1), args(2), args(3))
  }

  def apply(fullPath: String): NexusPath = {
    NexusPath(fullPath.split("/"))
  }

  def addSuffixToOrg(org: String, reconciledSuffix: String): String = {
    org + reconciledSuffix
  }
}
