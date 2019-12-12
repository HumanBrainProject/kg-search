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

export const getUpdatedUrl = (name, checked, value, many, location) => {
  const val = encodeURIComponent(value);
  let found = false;
  let counts = 0;
  const queryString = Object.entries(location.query).reduce((acc, [key, v]) => {
    const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/;// name[number]
    const isParamWithBracketsMatchingName = regParamWithBrackets.test(name) && name == key;
    const doesParamHasBrackets = !isParamWithBracketsMatchingName && regParamWithBrackets.test(key);
    const [, queryName] = doesParamHasBrackets?key.match(regParamWithBrackets):[null, key];
    const current = queryName === name && (!many || v === val );
    found = found || current;
    if (!current || checked) {
      let queryParam = "";
      if (queryName === name) { // same param name
        const queryValue = current ? val:v;
        if (many) {
          queryParam = `${queryName}[${counts}]=${queryValue}`;
          counts++;
        } else {
          queryParam = `${key}=${queryValue}`;
        }
      } else { // different param name
        queryParam = `${key}=${v}`;
      }
      acc += `${(acc.length > 1)?"&":""}${queryParam}`;
    }
    return acc;
  }, "?");

  let addParam = "";
  if (!found && checked) {
    const appendix = many?`[${counts}]`:"";
    addParam = `${(queryString.length > 1)?"&":""}${name}${appendix}=${val}`;
  }
  return `${location.pathname}${queryString}${addParam}`;
};

/*
export const getUpdatedUrl2 = (list, location) => {
  // name, checked, value, manyname, checked, value, many
  const items = list.reduce((acc, item) => {
    acc[item.name] = {
      ...item,
      key:
      value: encodeURIComponent(item.value),
      index: 0,
      found: false
    };
    return acc;
  }, {});
  const queryString = Object.entries(location.query).reduce((acc, [key, v]) => {
    const m = key.match(/^([^[]+)\[(\d+)\]$/); // name[number]
    const [, n, k] = m?m:[null, key, null];
    const item = items[n];
    const current = item && (!many || v ===  item.value );
    found = found || current;
    if (!current || checked) {
      const appendix = many?(n === name?`[${counts}]`:`[${k}]`):"";
      acc += `${(acc.length > 1)?"&":""}${n}${appendix}=${v}`;
      if (many && n === name) {
        counts++;
      }
    }
    return acc;
  }, "?");

  let addParam = "";
  if (!found && checked) {
    const appendix = many?`[${counts}]`:"";
    addParam = `${(queryString.length > 1)?"&":""}${name}${appendix}=${val}`;
  }
  return `${location.pathname}${queryString}${addParam}`;
};
*/
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