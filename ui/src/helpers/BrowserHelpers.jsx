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

export const searchToObj = () => {
  const search = window.location.search;
  if (search.length <= 1) {
    return {};
  }
  const urlParams = new URLSearchParams(search);
  const entries = urlParams.entries();
  const result = {};
  for(const [key, value] of entries) {
    const fixedOIDCKey = key.replace(/%5B/g, '[').replace(/%5D/g, ']');
    result[fixedOIDCKey] = value;
  }
  return result;
};

export const getHashKey = (key, hash) => {
  if (typeof key !== 'string') {
    return null;
  }
  if (typeof hash !== 'string') {
    hash = window.location.hash;
  }
  const patterns = [
    `^#${key}=([^&]+)&.+$`,
    `^.+&${key}=([^&]+)&.+$`,
    `^#${key}=([^&]+)$`,
    `^.+&${key}=([^&]+)$`
  ];
  let value = null;
  patterns.some(pattern => {
    const reg = new RegExp(pattern);
    const m = hash.match(reg);
    if (m && m.length === 2) {
      value = m[1];
      return true;
    }
    return false;
  });
  return value;
};

export const getUpdatedQuery = (query, name, checked, value, many) => {
  const result = {};
  const val = encodeURIComponent(value);
  let found = false;
  let counts = 0;
  Object.entries(query).forEach(([key, v]) => {
    const regParamWithBrackets = /^([^[]+)\[(\d+)\]$/;// name[number]
    const isParamWithBracketsMatchingName = regParamWithBrackets.test(name) && name === key;
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
    const appendix = many?`[${counts}]`:'';
    result[`${name}${appendix}`] =  val;
  }
  return result;
};

export const getLocationSearchFromQuery = query => Object.entries(query).reduce((acc, [key, value]) => {
  acc += `${(acc.length > 0)?'&':'?'}${key}=${value}`;
  return acc;
}, '');

export const windowHeight = () => {
  const w = window,
    d = document,
    e = d.documentElement,
    g = d.getElementsByTagName('body')[0];
  return w.innerHeight || e.clientHeight || g.clientHeight;
};

export const windowWidth = () => {
  const w = window,
    d = document,
    e = d.documentElement,
    g = d.getElementsByTagName('body')[0];
  return w.innerWidth || e.clientWidth || g.clientWidth;
};

export const isMobile = (navigator.userAgent.match(/Android/i)
                || navigator.userAgent.match(/webOS/i)
                || navigator.userAgent.match(/iPhone/i)
                || navigator.userAgent.match(/iPad/i)
                || navigator.userAgent.match(/iPod/i));

export const tabAblesSelectors = [
  'input',
  'select',
  'a[href]',
  'textarea',
  'button',
  '[tabindex]',
];

export const isOpera = (navigator.userAgent.indexOf('Opera') || navigator.userAgent.indexOf('OPR')) !== -1;
export const isChrome = navigator.userAgent.indexOf('Chrome') !== -1;
export const isSafari = navigator.userAgent.indexOf('Safari') !== -1;
export const isFirefox = navigator.userAgent.indexOf('Firefox') !== -1;
export const isIE = navigator.userAgent.indexOf('MSIE') !== -1  || !!document.documentMode;