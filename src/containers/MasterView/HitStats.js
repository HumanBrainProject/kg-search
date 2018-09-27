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

import { connect } from "../../store";
import { HitStats as Component } from "../../components/HitStats";

export const HitStats = connect(
  state => {
    const from = (state.search.from?state.search.from:0) + 1;
    const count = state.search.results?(state.search.results.hits?(state.search.results.hits.hits?state.search.results.hits.hits.length:0):0):0;
    const to = from + count - 1;
    return {
      show: state.search.isReady && state.search.initialRequestDone && !state.search.isLoading,
      hitCount: state.search.results?(state.search.results.hits?state.search.results.hits.total:0):0,
      from: from,
      to: to
    };
  }
)(Component);