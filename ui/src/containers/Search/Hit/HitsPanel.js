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

import React from "react";
import uniqueId from "lodash/uniqueId";
import { connect } from "react-redux";
import * as actionsInstances from "../../../actions/actions.instances";
import { HitsList } from "../../../components/HitsList/HitsList";
import { Hit } from "./Hit";
import { StatsHelpers } from "../../../helpers/StatsHelpers";


const HitsPanelBase = ({ lists, itemComponent, getKey, onClick }) => (
  <React.Fragment>
    {lists.map(list =>
      <HitsList key={list.id} title={list.title} items={list.items} itemComponent={itemComponent} getKey={getKey} onClick={onClick} />
    )}
  </React.Fragment>
);


const groupHitsByField = (hits, field) => {
  return Object.values(hits.reduce((acc, hit, index) => {
    const f = hit._source && hit._source[field];
    const value = f && !Array.isArray(f) && (typeof f === "object"?f.value:value);
    //console.log(value, value.startsWith("Probabilistic cytoarchitectonic"));
    if (!value) {
      acc[uniqueId()] = {
        hits: [hit],
        index: index
      };
      return acc;
    }
    const reg = /(Probabilistic cytoarchitectonic map of\s[^\s]+)\s.*/;
    const [, test] = (typeof value === "string" && reg.test(value))?value.match(reg):[null, null];
    const key = test?test: uniqueId(); // TODO: remove test and use value
    if (!acc[key]) {
      acc[key] = {
        hits: [hit],
        index: index
      };
    } else {
      acc[key].hits.push(hit);
    }
    return acc;
  }, {})).sort((a, b) => a.index < b.index).map(group => group.hits.length == 1?group.hits[0]:group.hits);
};

const groupHitsByScore = hits => {
  let trySplitResult = true;
  let isSortedByRelevance = false;
  try {
    const params = window.location.search
      .substring(1)
      .split("&")
      .map(s => s.split("="))
      .reduce((obj, a) => {
        obj[a[0]] = a[1];
        return obj;
      }, {});
    const page = params["p"];
    const sort = params["sort"];
    typeof sort === "undefined" || sort === "newestFirst" ? isSortedByRelevance = true : isSortedByRelevance = false;
    trySplitResult = !(page && page !== "1") && !(sort && sort !== "_score_desc");
  } catch (e) {
    // window.console.debug("Failed to calculate stats");
  }

  let limit = -1;
  if (trySplitResult) {
    try {
      const values = hits.reduce((acc, hit) => {
        if (Array.isArray(hit)) {
          acc.push(...hit.map(h => h._score));
        } else {
          acc.push(hit._score);
        }
        return acc;
      }, []);
      const average = StatsHelpers.average(values);
      const standardDeviation = StatsHelpers.standardDeviation(values);
      limit = average + 2 * standardDeviation;
      if (standardDeviation < 10) {
        limit = -1;
      }
      // window.console.debug(" average: " + average + ", standard deviation: " + standardDeviation + ", limit: " + limit);
    } catch (e) {
      // window.console.debug("Failed to calculate stats");
    }
  }

  const topMatchHits = [];
  const moreHits = [];
  isSortedByRelevance ?
    hits.forEach(hit => {
      let score = 0;
      if (Array.isArray(hit)) {
        score = hit.reduce((acc, h) => h._score > acc?h.score:acc, 0);
      } else {
        score = hit._score;
      }
      if (limit !== -1 && score > limit) {
        topMatchHits.push(hit);
      } else {
        moreHits.push(hit);
      }
    }) :
    hits.forEach(hit => {
      topMatchHits.push(hit);
    });

  return [
    {
      id: 0,
      title: null,
      items: topMatchHits
    },
    {
      id: 1,
      title: (topMatchHits.length && moreHits.length) ? "Other results" : null,
      items: moreHits
    }
  ];
};

const mapStateToProps = state => {
  const test = groupHitsByField(state.search.hits, "title");
  window.console.log(state.search.hits, test);
  return {
    lists: groupHitsByScore(groupHitsByField(state.search.hits, "title")),
    itemComponent: Hit,
    getKey: data => `${data?._source?.type?.value}/${data._id ? data._id : uniqueId()}`
  };
};

export const HitsPanel = connect(
  mapStateToProps,
  dispatch => ({
    onClick: (data, target) => {
      dispatch(actionsInstances.setInstance(data, target));
      dispatch(actionsInstances.updateLocation());
    }
  })
)(HitsPanelBase);