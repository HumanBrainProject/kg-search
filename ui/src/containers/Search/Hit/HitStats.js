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
import { useLocation, useNavigate } from "react-router-dom";
import ReactPiwik from "react-piwik";

import * as actionsSearch from "../../../actions/actions.search";

import "./HitStats.css";

export const Suggestion =  ({word, isLast=true, onClick}) => {

  const location = useLocation();
  const navigate = useNavigate();

  const handleOnClick = () => onClick(word, location, navigate);

  return (
    <li><button className="kgs-suggestion__btn" role="link" onClick={handleOnClick} title={`search for ${word}`}>{word}</button>{isLast?"":" or "}</li>
  );
};

export const Suggestions =  ({words, onClick}) => (
  <ul className="kgs-suggestions">
    {words.map((word, idx) => (
      <Suggestion key={word} word={word} isLast={idx === words.length -1} onClick={onClick} />
    ))}
  </ul>
);

export const HitStatsBase = ({show, message, suggestions, hitCount, from, to, setQueryString}) => {
  if (!show) {
    return null;
  }
  if (message) {
    return (
      <span className="kgs-hitStats">{message}</span>
    );
  }
  if (hitCount === 0) {
    if (suggestions.length) {
      return (
        <span className="kgs-hitStats no-hits">No results were found. Did you mean <Suggestions words={suggestions} onClick={setQueryString} />?</span>
      );
    }
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
      suggestions: state.search.suggestions,
      hitCount: state.search.total?state.search.total:0,
      from: from,
      to: to
    };
  },
  dispatch => ({
    setQueryString: (value, location, navigate) => {
      ReactPiwik.push(["trackEvent", "Search", "Refine search using suggestion", value]);
      let to = "/";
      const find = location.search.split("&").find(p => p.match(/\??q=.*/));
      if (find) {
        to = location.search.replace(find, `${find.startsWith("?")?"?":""}q=${encodeURIComponent(value)}`);
      } else {
        to = location.search + location.search.endsWith("?")?"q=":"&q=" + encodeURIComponent(value);
      }
      navigate(to);
      dispatch(actionsSearch.setQueryString(value));
    }
  })
)(HitStatsBase);