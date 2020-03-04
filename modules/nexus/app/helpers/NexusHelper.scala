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

import java.security.MessageDigest

import play.api.http.Status._
import play.api.libs.json._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

object NexusHelper {

  val minimalSchemaDefinition = """
    {
      "@type": "owl:Ontology",
      "@context": {
          "datatype": {
              "@id": "sh:datatype",
              "@type": "@id"
          },
          "name": "sh:name",
          "path": {
              "@id": "sh:path",
              "@type": "@id"
          },
          "property": {
              "@id": "sh:property",
              "@type": "@id"
          },
          "targetClass": {
              "@id": "sh:targetClass",
              "@type": "@id"
          },
          "schema": "http://schema.org/",
          "sh": "http://www.w3.org/ns/shacl#",
          "owl": "http://www.w3.org/2002/07/owl#",
          "xsd": "http://www.w3.org/2001/XMLSchema#",
          "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
          "org": "${nameSpace}",
          "shapes": {
              "@reverse": "rdfs:isDefinedBy",
              "@type": "@id"
          }
      },
      "shapes": [
        {
          "@id": "org:${entityType}Shape",
          "@type": "sh:NodeShape",
          "property": [
            {
              "datatype": "xsd:string",
              "path": "schema:identifier"
            }
          ],
          "targetClass": "org:${entityType}"
        }
      ]
    }
    """

  val schemaDefinitionForEditor = """
    {
      "@type": "owl:Ontology",
      "@context": {
          "datatype": {
              "@id": "sh:datatype",
              "@type": "@id"
          },
          "name": "sh:name",
          "path": {
              "@id": "sh:path",
              "@type": "@id"
          },
          "property": {
              "@id": "sh:property",
              "@type": "@id"
          },
          "targetClass": {
              "@id": "sh:targetClass",
              "@type": "@id"
          },
          "${org}": "http://hbp.eu/${org}#",
          ${editorContext}
          "schema": "http://schema.org/",
          "sh": "http://www.w3.org/ns/shacl#",
          "owl": "http://www.w3.org/2002/07/owl#",
          "xsd": "http://www.w3.org/2001/XMLSchema#",
          "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
          "shapes": {
              "@reverse": "rdfs:isDefinedBy",
              "@type": "@id"
          }
      },
      "shapes": [
        {
          "@id": "${org}:${entityType}Shape",
          "@type": "sh:NodeShape",
          "property": [
            {
              "datatype": "xsd:string",
              "path": "${editorOrg}:origin"
            }
          ],
          "targetClass": "${org}:${entityType}"
        }
      ]
    }
    """

  def domainDefinition(description: String): JsObject = {
    Json.obj(
      "description" -> description
    )
  }

  def hash(payload: String): String = {
    MessageDigest.getInstance("MD5").digest(payload.getBytes).map(0xFF & _).map { "%02x".format(_) }.foldLeft("") {
      _ + _
    }
  }

}
