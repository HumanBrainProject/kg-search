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
import { connect } from "react-redux";
import "./HitStats.css";

export const HitStatsBase = ({show, message, hitCount, from, to}) => {
  if (!show) {
    return null;
  }
  if (message) {
    return (
      <span className="kgs-hitStats">{message}</span>
    );
  }
  if (hitCount === 0) {
    return (
      <span className="kgs-hitStats no-hits">No results were found. Please refine your search.</span>
    );
  }
  return (
    <span className="kgs-hitStats">Viewing <span className="kgs-hitStats-highlight">{from}-{to}</span> of <span className="kgs-hitStats-highlight">{hitCount}</span> results</span>
  );
};

export const HitStats = connect(
  state => {
    const from = (state.search.from?state.search.from:0) + 1;
    const count = state.search.hits?state.search.hits.length:0;
    const to = from + count - 1;
    return {
      show: !state.search.isLoading,
      message: state.search.message,
      hitCount: state.search.total?state.search.total:0,
      from: from,
      to: to
    };
  }
)(HitStatsBase);