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

const getUpdatedQuery = (query, name, checked, value, many) => {
  const result = {};
  const val = encodeURIComponent(value);
  let found = false;
  let counts = 0;
  Object.entries(query).forEach(([key, v]) => {
    const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/;// name[number]
    const isParamWithBracketsMatchingName = regParamWithBrackets.test(name) && name == key;
    const doesParamHasBrackets = !isParamWithBracketsMatchingName && regParamWithBrackets.test(key);
    const [, queryName] = doesParamHasBrackets?key.match(regParamWithBrackets):[null, key];
    const current = queryName === name && (!many || v === val );
    found = found || current;
    if (!current || checked) {
      if (queryName === name) { // same param name
        const queryValue = current ? val:v;
        if (many) {
          result[`${queryName}[${counts}]`] =  queryValue;
          counts++;
        } else {
          result[key] = queryValue;
        }
      } else { // different param name
        result[key] = v;
      }
    }
  });

  if (!found && checked) {
    const appendix = many?`[${counts}]`:"";
    result[`${name}${appendix}`] =  val;
  }
  return result;
};

const getLocationFromQuery = (query, location) => {
  const queryString = Object.entries(query).reduce((acc, [key, value]) => {
    acc += `${(acc.length > 1)?"&":""}${key}=${value}`;
    return acc;
  }, "?");
  return `${location.pathname}${queryString}`;
};

export const getUpdatedUrl = (name, checked, value, many, location) => {
  const query = getUpdatedQuery(location.query, name, checked, value, many);
  return getLocationFromQuery(query, location);
};

export const getUpdatedUrlForList = (list, location) => {
  const query = list.reduce((acc, item) => getUpdatedQuery(acc, item.name, item.checked, item.value, item.many), location.query);
  return getLocationFromQuery(query, location);
};

export const windowHeight = () => {
  const w = window,
    d = document,
    e = d.documentElement,
    g = d.getElementsByTagName("body")[0];
  return w.innerHeight || e.clientHeight || g.clientHeight;
  //return $(window).height();
};

export const windowWidth = () => {
  const w = window,
    d = document,
    e = d.documentElement,
    g = d.getElementsByTagName("body")[0];
  return w.innerWidth || e.clientWidth || g.clientWidth;
  //return $(window).height();
};

export const isMobile = (navigator.userAgent.match(/Android/i)
                || navigator.userAgent.match(/webOS/i)
                || navigator.userAgent.match(/iPhone/i)
                || navigator.userAgent.match(/iPad/i)
                || navigator.userAgent.match(/iPod/i));

export const tabAblesSelectors = [
  "input",
  "select",
  "a[href]",
  "textarea",
  "button",
  "[tabindex]",
];

export const isOpera = (navigator.userAgent.indexOf("Opera") || navigator.userAgent.indexOf("OPR")) !== -1;
export const isChrome = navigator.userAgent.indexOf("Chrome") !== -1;
export const isSafari = navigator.userAgent.indexOf("Safari") !== -1;
export const isFirefox = navigator.userAgent.indexOf("Firefox") !== -1;
export const isIE = navigator.userAgent.indexOf("MSIE") !== -1  || !!document.documentMode;