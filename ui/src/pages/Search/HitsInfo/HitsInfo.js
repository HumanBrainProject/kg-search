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
import { useSelector, useDispatch } from "react-redux";
import { useLocation, useNavigate } from "react-router-dom";

import { trackEvent } from "../../../app/services/api";
import { setQueryString } from "../../../features/search/searchSlice";

import "./HitsInfo.css";

const Suggestion =  ({word, searchTerm, isSecondLast, isLast=true }) => {

  const location = useLocation();
  const navigate = useNavigate();

  const dispatch = useDispatch();

  const getSuggestionToUrl = () => {
    const find = location.search.split("&").find(p => p.match(/\??q=.*/));
    if (find) {
      return location.search.replace(find, `${find.startsWith("?")?"?":""}q=${encodeURIComponent(searchTerm)}`);
    }

    return location.search + location.search.endsWith("?")?"q=":"&q=" + encodeURIComponent(searchTerm);
  };

  const handleOnClick = () => {
    trackEvent("Search", "Refine search using suggestion", searchTerm);
    const to = getSuggestionToUrl();
    navigate(to);
    dispatch(setQueryString(searchTerm));
  };

  const getSeparator = () => {
    if (isSecondLast) {
      return " or ";
    }
    if (isLast) {
      return "";
    }
    return ", ";
  };

  const separator = getSeparator();

  return (
    <li><button className="kgs-suggestion__btn" role="link" onClick={handleOnClick} title={`search for ${word}`}>{word}</button>{separator}</li>
  );
};

const Suggestions =  ({ words }) => {
  const list = Object.entries(words);
  return (
    <ul className="kgs-suggestions">
      {list.map(([word, searchTerm], idx) => (
        <Suggestion key={word} word={word} searchTerm={searchTerm} isSecondLast={idx === (list.length-2)} isLast={idx === (list.length-1)} />
      ))}
    </ul>
  );
};

const Viewing = ({ hitCount, from, to }) => (
  <>
    Viewing <span className="kgs-hitsInfos-highlight">{from}-{to}</span> of <span className="kgs-hitsInfos-highlight">{hitCount}</span> results.
  </>
);

const HitsInfo = () => {

  const isFetching = useSelector(state => state.search.isFetching);
  const from = useSelector(state => (state.search.from?state.search.from:0) + 1);
  const count = useSelector(state => state.search.hits?state.search.hits.length:0);
  const to = from + count - 1;
  const message = useSelector(state => state.search.message);
  const suggestions = useSelector(state => state.search.suggestions);
  const hitCount = useSelector(state => state.search.total?state.search.total:0);

  if (isFetching && hitCount === 0) {
    return null;
  }
  if (message) {
    return (
      <span className="kgs-hitsInfos">{message}</span>
    );
  }

  if (Object.keys(suggestions).length>0) {
    return (
      <span className="kgs-hitsInfos">
        {hitCount === 0?
          "No results were found. "
          :
          <Viewing hitCount={hitCount} from={from} to={to} />
        }<br/>Did you mean <Suggestions words={suggestions} />?</span>
    );
  }
  if (hitCount === 0) {
    return (
      <span className="kgs-hitsInfos">No results were found. Please refine your search.</span>
    );
  }
  if (from > hitCount) {
    return (
      <span className="kgs-hitsInfos">No results were found. Only {hitCount} results are availalbe. Please navigate to previous page(s).</span>
    );
  }
  return (
    <span className="kgs-hitsInfos"><Viewing hitCount={hitCount} from={from} to={to} /></span>
  );
};

export default HitsInfo;