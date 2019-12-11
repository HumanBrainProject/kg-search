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
import * as actions from "../../actions";
import { List } from "../../components/List";
import { Hit } from "./Hit";
import { StatsHelpers } from "../../helpers/StatsHelpers";


const HitsPanelBase = ({ lists, itemComponent, getKey, layout, onClick }) => (
  <React.Fragment>
    {lists.map(list =>
      <List key={list.id} title={list.title} items={list.items} itemComponent={itemComponent} getKey={getKey} layout={layout} onClick={onClick} />
    )}
  </React.Fragment>
);

const mapStateToProps = (state, props) => {
  //const { hits } = props;
  const hits = state.search.hits;

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
      const values = hits.map(hit => hit._score);
      const average = StatsHelpers.average(values);
      const standardDeviation = StatsHelpers.standardDeviation(values);
      limit = average + 2 * standardDeviation;
      //window.console.debug("average: " + average + ", standard deviation: "  + standardDeviation + ", limit: " + limit);
    } catch (e) {
      // window.console.debug("Failed to calculate stats");
    }
  }

  const topMatchHits = [];
  const moreHits = [];
  isSortedByRelevance ?
    hits.forEach(hit => {
      if (limit !== -1 && hit._score > limit) {
        topMatchHits.push(hit);
      } else {
        moreHits.push(hit);
      }
    }) :
    hits.forEach(hit => {
      topMatchHits.push(hit);
    });

  return {
    lists: [
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
    ],
    itemComponent: Hit,
    getKey: data => `${data._type ? data._type : "unknown"}/${data._id ? data._id : uniqueId()}`,
    layout: state.application.gridLayoutMode ? "grid" : "list"
  };
};

export const HitsPanel = connect(
  mapStateToProps,
  dispatch => ({
    onClick: (data, target) => {
      dispatch(actions.setInstance(data, target));
    }
  })
)(HitsPanelBase);