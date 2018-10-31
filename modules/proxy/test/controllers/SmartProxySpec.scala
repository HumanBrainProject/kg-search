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
import akka.util.ByteString
import controllers.SearchProxy
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, Json}

class SmartProxySpec extends PlaySpec {

  "smartProxy" should {
    "adapt json request" in {
      val query = """
        {
          "query": {
            "bool": {
              "filter": [
                {
                  "term": {
                    "_type": "Dataset"
                  }
                }
              ]
            }
          },
          "aggs": {
            "facet_Dataset_subjects.children.species8": {
              "filter": {
                "term": {
                  "_type": "Dataset"
                }
              },
              "aggs": {
                "inner": {
                  "nested": {
                    "path": "subjects.children"
                  },
                  "aggs": {
                    "subjects.children.species.value.keyword": {
                      "terms": {
                        "field": "subjects.children.species.value.keyword",
                        "size": 10,
                        "order": {
                          "_count": "desc"
                        }
                      }
                    },
                    "subjects.children.species.value.keyword_count": {
                      "cardinality": {
                        "field": "subjects.children.species.value.keyword"
                      }
                    }
                  }
                }
              }
            },
            "facet_Dataset_subjects.children.species10": {
              "filter": {
                "term": {
                  "_type": "Dataset"
                }
              },
              "aggs": {
                "inner": {
                  "nested": {
                    "path": "subjects.children"
                  },
                  "aggs": {
                    "subjects.children.species.value.keyword": {
                      "terms": {
                        "field": "subjects.children.species.value.keyword",
                        "size": 10,
                        "order": {
                          "_count": "desc"
                        }
                      }
                    },
                    "subjects.children.species.value.keyword_count": {
                      "cardinality": {
                        "field": "subjects.children.species.value.keyword"
                      }
                    }
                  }
                }
              }
            }
          },
          "size": 20,
          "sort": [
            {
              "_score": "desc"
            }
          ],
          "highlight": {
            "fields": {
              "title.value": {},
              "description.value": {},
              "contributors.value": {},
              "owners.value": {},
              "component.value": {},
              "created_at.value": {},
              "releasedate.value": {},
              "activities.value": {}
            },
            "encoder": "html"
          }
        }
        """

      val jsExpected = Json.parse(
        """
        {
          "query": {
            "bool": {
              "filter": [
                {
                  "term": {
                    "_type": "Dataset"
                  }
                }
              ]
            }
          },
          "aggs": {
            "facet_Dataset_subjects.children.species8": {
              "filter": {
                "term": {
                  "_type": "Dataset"
                }
              },
              "aggs": {
                "inner": {
                  "nested": {
                    "path": "subjects.children"
                  },
                  "aggs": {
                    "subjects.children.species.value.keyword": {
                      "terms": {
                        "field": "subjects.children.species.value.keyword",
                        "size": 10,
                        "order": {
                          "_count": "desc"
                        }
                      },
                      "aggs": {
                        "temporary_parent_doc_count": {
                          "reverse_nested": {}
                        }
                      }
                    },
                    "subjects.children.species.value.keyword_count": {
                      "cardinality": {
                        "field": "subjects.children.species.value.keyword"
                      }
                    }
                  }
                }
              }
            },
            "facet_Dataset_subjects.children.species10": {
              "filter": {
                "term": {
                  "_type": "Dataset"
                }
              },
              "aggs": {
                "inner": {
                  "nested": {
                    "path": "subjects.children"
                  },
                  "aggs": {
                    "subjects.children.species.value.keyword": {
                      "terms": {
                        "field": "subjects.children.species.value.keyword",
                        "size": 10,
                        "order": {
                          "_count": "desc"
                        }
                      },
                      "aggs": {
                        "temporary_parent_doc_count": {
                          "reverse_nested": {}
                        }
                      }
                    },
                    "subjects.children.species.value.keyword_count": {
                      "cardinality": {
                        "field": "subjects.children.species.value.keyword"
                      }
                    }
                  }
                }
              }
            }
          },
          "size": 20,
          "sort": [
            {
              "_score": "desc"
            }
          ],
          "highlight": {
            "fields": {
              "title.value": {},
              "description.value": {},
              "contributors.value": {},
              "owners.value": {},
              "component.value": {},
              "created_at.value": {},
              "releasedate.value": {},
              "activities.value": {}
            },
            "encoder": "html"
          }
        }
        """).as[JsObject]

      val jsUp = Json.parse(SearchProxy.adaptEsQueryForNestedDocument(ByteString(query.getBytes)).utf8String)
      assert(jsUp.equals(jsExpected))

    }

    "adapt json response" in {

      val js = Json.parse(
        """
          {
            "took": 90,
            "timed_out": false,
            "_shards": {
              "total": 5,
              "successful": 5,
              "skipped": 0,
              "failed": 0
            },
            "aggregations": {
              "facet_Dataset_subjects.children.species8": {
                "doc_count": 133,
                "inner": {
                  "doc_count": 816,
                  "subjects.children.species.value.keyword": {
                    "doc_count_error_upper_bound": 0,
                    "sum_other_doc_count": 0,
                    "buckets": [
                      {
                        "key": "Homo sapiens",
                        "doc_count": 801,
                        "temporary_parent_doc_count": {
                          "doc_count": 127
                        }
                      },
                      {
                        "key": "Mus musculus",
                        "doc_count": 13,
                        "temporary_parent_doc_count": {
                          "doc_count": 5
                        }
                      },
                      {
                        "key": "Macaca mulatta",
                        "doc_count": 2,
                        "temporary_parent_doc_count": {
                          "doc_count": 1
                        }
                      }
                    ]
                  },
                  "subjects.children.species.value.keyword_count": {
                    "value": 3
                  }
                }
              },
              "facet_Dataset_subjects.children.species10": {
                 "doc_count": 102,
                 "inner": {
                   "doc_count": 555,
                   "subjects.children.species.value.keyword": {
                     "doc_count_error_upper_bound": 0,
                     "sum_other_doc_count": 0,
                     "buckets": [
                       {
                         "key": "Homo sapiens",
                         "doc_count": 666,
                         "temporary_parent_doc_count": {
                           "doc_count": 66
                         }
                       },
                       {
                         "key": "Mus musculus",
                         "doc_count": 777,
                         "temporary_parent_doc_count": {
                           "doc_count": 4
                         }
                       },
                       {
                         "key": "Macaca mulatta",
                         "doc_count": 888,
                         "temporary_parent_doc_count": {
                           "doc_count": 2
                         }
                       },
                       {
                          "key": "Lambda Species",
                          "doc_count": 999,
                          "temporary_parent_doc_count": {
                            "doc_count": 1
                          }
                        }
                     ]
                   },
                   "subjects.children.species.value.keyword_count": {
                     "value": 3
                   }
                 }
               }
            }
          }
        """).as[JsObject]

      val jsExpected = Json.parse(
        """
          {
            "took": 90,
            "timed_out": false,
            "_shards": {
              "total": 5,
              "successful": 5,
              "skipped": 0,
              "failed": 0
            },
            "aggregations": {
              "facet_Dataset_subjects.children.species8": {
                "doc_count": 133,
                "inner": {
                  "doc_count": 133,
                  "subjects.children.species.value.keyword": {
                    "doc_count_error_upper_bound": 0,
                    "sum_other_doc_count": 0,
                    "buckets": [
                      {
                        "key": "Homo sapiens",
                        "doc_count": 127
                      },
                      {
                        "key": "Mus musculus",
                        "doc_count": 5
                      },
                      {
                        "key": "Macaca mulatta",
                        "doc_count": 1
                      }
                    ]
                  },
                  "subjects.children.species.value.keyword_count": {
                    "value": 3
                  }
                }
              },
              "facet_Dataset_subjects.children.species10": {
                 "doc_count": 102,
                 "inner": {
                   "doc_count": 73,
                   "subjects.children.species.value.keyword": {
                     "doc_count_error_upper_bound": 0,
                     "sum_other_doc_count": 0,
                     "buckets": [
                       {
                         "key": "Homo sapiens",
                         "doc_count": 66
                       },
                       {
                         "key": "Mus musculus",
                         "doc_count": 4
                       },
                       {
                         "key": "Macaca mulatta",
                         "doc_count": 2
                       },
                       {
                          "key": "Lambda Species",
                          "doc_count": 1
                       }
                     ]
                   },
                   "subjects.children.species.value.keyword_count": {
                     "value": 3
                   }
                 }
               }
            }
          }
        """).as[JsObject]

      val jsUp = SearchProxy.updateEsResponseWithNestedDocument(js)
      assert(jsUp.equals(jsExpected))
    }
  }
}