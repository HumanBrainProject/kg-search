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
import { withStoreStateSubscription} from "../../../withStoreStateSubscription";
import "./styles.css";

const HitStatsPanelComponent = ({show, hitCount, from, to}) => {
  if (!show) {
    return null;
  }
  if (hitCount === 0) {
    return (
      <span className="kgs-hitStats">No results were found. Please refine your search.</span>
    );
  }
  return (
    <span className="kgs-hitStats">Viewing <span className="kgs-hitStats-highlight">{from}-{to}</span> of <span className="kgs-hitStats-highlight">{hitCount}</span> results</span>
  );
};

export const HitStatsPanel = withStoreStateSubscription(
  HitStatsPanelComponent,
  data => {
    const from = (data.search.from?data.search.from:0) + 1;
    const count = data.search.results?(data.search.results.hits?(data.search.results.hits.hits?data.search.results.hits.hits.length:0):0):0;
    const to = from + count - 1;
    return {
      show: data.search.isReady && data.search.initialRequestDone && !data.search.isLoading,
      hitCount: data.search.results?(data.search.results.hits?data.search.results.hits.total:0):0,
      from: from,
      to: to
    };
  }
);
