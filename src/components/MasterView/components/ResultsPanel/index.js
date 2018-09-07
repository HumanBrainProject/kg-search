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
import { SearchkitComponent, Hits } from "searchkit";
import { dispatch } from "../../../../store";
import * as actions from "../../../../actions";
import { withStoreStateSubscription} from "../../../withStoreStateSubscription";
import { Hit } from "../Hit";
import { StatsHelpers } from "../../../../Helpers/StatsHelpers";
import "./styles.css";

function HitItem({bemBlocks, data}) {

  const openDetail = (event) => {
    dispatch(actions.setHit(data, event.currentTarget));
  };

  return (
    <li className={bemBlocks.item().mix(bemBlocks.container("item"))} >
      <button role="link" onClick={openDetail} data-type={data._type} data-id={data._id}>
        <Hit data={data} />
        <span className={bemBlocks.item("chevron")}><i className="fa fa-chevron-right"></i></span>
      </button>
    </li>
  );
}

class HitList extends SearchkitComponent {
  defineBEMBlocks() {
    const blockName = "sk-hits-list";
    return {
      container: blockName,
      item: `${blockName}-hit`
    };
  }
  render() {
    const {hits} = this.props;
    const bemBlocks = this.bemBlocks;

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
    hits.forEach((hit, index) => {
      if (hit) {
        const item = <HitItem bemBlocks={bemBlocks} data={hit} key={hit._type?(hit._type + "/" + hit._id):index} />;
        if (limit !== -1 && hit._score >= limit) {
          topMatchHits.push(item);
        } else {
          moreHits.push(item);
        }
      }
    });

    const moreHitsTitle = (topMatchHits.length && moreHits.length)?<li className="kgs-other-result-title">Other results </li>:null;

    return (
      <ul className={bemBlocks.container()}>
        {topMatchHits}
        {moreHitsTitle}
        {moreHits}
      </ul>
    );
  }
}

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
data.definition.queryFields.forEach(field => {
  highlights[field.replace(/^(.*?)\^.*$/g,"$1")] = {};
});
*/
export const ResultsPanel = withStoreStateSubscription(
  ResultsPanelComponent,
  data => ({
    gridLayoutMode: data.application.gridLayoutMode,
    hitsPerPage: data.configuration.hitsPerPage
  })
);