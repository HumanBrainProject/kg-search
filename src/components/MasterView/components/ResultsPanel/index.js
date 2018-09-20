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
import { Hits } from "searchkit";
import { connect } from "../../../../store";
import * as actions from "../../../../actions";
import { Hit } from "../Hit";
import { StatsHelpers } from "../../../../Helpers/StatsHelpers";
import "./styles.css";

const HitItem = ({data, onClick}) => {

  const handleClick = (event) => {
    onClick(data, event.currentTarget);
  };

  return (
    <li>
      <button role="link" onClick={handleClick} data-type={data._type} data-id={data._id}>
        <Hit data={data} />
        <span className="kgs-hit__chevron"><i className="fa fa-chevron-right"></i></span>
      </button>
    </li>
  );
};

const HitListComponent = ({title, hits, onClick}) => {
  if (!Array.isArray(hits) || hits.length === 0) {
    return null;
  }

  return (
    <div className="kgs-hits">
      {title && (
        <div>{title}</div>
      )}
      <ul>
        {hits.map((hit, index) => (
          <HitItem key={hit._type?(hit._type + "/" + hit._id):index} data={hit} onClick={onClick} />
        ))}
      </ul>
    </div>
  );
};

const HitListPanel = ({topMatchHits, moreHits, moreHitsTitle, onClick}) => (
  <span>
    <HitListComponent hits={topMatchHits} onClick={onClick} />
    <HitListComponent hits={moreHits} onClick={onClick} title={moreHitsTitle} />
  </span>
);

const mapStateToProps = (state, props) => {
  const {hits} = props;

  let trySplitResult = true;
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
    const sort = params["sort"]; //
    trySplitResult = !(page && page !== "1") && !(sort && sort !== "_score_desc");
  } catch (e) {
    window.console.debug("Failed to calculate stats");
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
      window.console.debug("Failed to calculate stats");
    }
  }

  const topMatchHits = [];
  const moreHits = [];
  hits.forEach(hit => {
    if (limit !== -1 && hit._score >= limit) {
      topMatchHits.push(hit);
    } else {
      moreHits.push(hit);
    }
  });

  return {
    topMatchHits: topMatchHits,
    moreHits: moreHits,
    moreHitsTitle: (topMatchHits.length && moreHits.length)?"Other results":null
  };
};

export const HitList = connect(
  mapStateToProps,
  dispatch => ({
    onClick: (data, target) => dispatch(actions.setHit(data, target))
  })
)(HitListPanel);

const ResultsPanelComponent = ({gridLayoutMode, hitsPerPage}) => {
  //window.console.debug("ResultsPanel rendering...");

  const highlights = {
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
  };

  /*
  <NoHits translations={{
          "NoHits.NoResultsFound":"No results were found for {query}",
          "NoHits.DidYouMean":"Search for {suggestion}",
          "NoHits.SearchWithoutFilters":"Search for {query} without filters"
        }} suggestionsField="all"/>
  */
  return (
    <span className={`kgs-result-layout ${gridLayoutMode?"is-grid":"is-list"}`}>
      <Hits customHighlight={highlights} hitsPerPage={hitsPerPage} listComponent={HitList} scrollTo="body" />
    </span>
  );
};

/*
const highlights = {};
state.definition.queryFields.forEach(field => {
  highlights[field.replace(/^(.*?)\^.*$/g,"$1")] = {};
});
*/
export const ResultsPanel = connect(
  state => ({
    gridLayoutMode: state.application.gridLayoutMode,
    hitsPerPage: state.configuration.hitsPerPage
  })
)(ResultsPanelComponent);