/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */

import React from "react";
import uniqueId from "lodash/uniqueId";
import { connect } from "react-redux";
import ReactPiwik from "react-piwik";

import * as actionsInstances from "../../../actions/actions.instances";
import { HitsList } from "../../../components/HitsList/HitsList";
import { Hit } from "./Hit";
import { StatsHelpers } from "../../../helpers/StatsHelpers";
import { useNavigate } from "react-router-dom";


const HitsPanelBase = ({ lists, itemComponent, getKey, group, onClick }) => {
  const navigate = useNavigate();

  const handleClick = (data, target) => onClick(data, target, group, navigate);

  return (
    <React.Fragment>
      {lists.map(list =>
        <HitsList key={list.id} title={list.title} items={list.items} itemComponent={itemComponent} getKey={getKey} onClick={handleClick} />
      )}
    </React.Fragment>
  );
};


const mapStateToProps = state => {
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
    getKey: data => `${data?._source?.type?.value}/${data._id ? data._id : uniqueId()}`,
    group: state.groups.group !== state.groups.defaultGroup?state.groups.group:null
  };
};

export const HitsPanel = connect(
  mapStateToProps,
  dispatch => ({
    onClick: (data, target, group, navigate) => {
      if (target === "_blank") {
        const relativeUrl = `/instances/${data._id}${group?("?group=" + group):""}`;
        ReactPiwik.push(["trackEvent", "Card", "Open in new tab", relativeUrl]);
        window.open(relativeUrl, "_blank");
      } else {
        dispatch(actionsInstances.setInstance(data));
        navigate(`/${window.location.search}#${data._id}`);
      }
    }
  })
)(HitsPanelBase);