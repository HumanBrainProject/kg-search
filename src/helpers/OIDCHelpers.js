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

export const generateKey = () => {
  let key = "";
  const chars = "ABCDEF0123456789";
  for (let i=0; i<4; i++) {
    if (key !== "") {
      key += "-";
    }
    for (let j=0; j<5; j++) {
      key += chars.charAt(Math.floor(Math.random() * chars.length));
    }
  }
  return key;
};

const regParam = /^(.+)=(.+)$/;

export const searchToObj = search => {
  if (typeof search !== "string") {
    search = window.location.search;
  }
  if (search.length <= 1) {
    return {};
  }
  return search.replace(/^\??(.*)$/, "$1").split("&").reduce((result, param) => {
    if (param === "" || !regParam.test(param)) {
      return result;
    }
    const [ , key, value] = param.match(regParam);
    result[key] = value;
    return result;
  }, {});
};

export const getSearchKey = (key, ignoreCase=false, search) => {
  if (typeof key !== "string") {
    return null;
  }
  if (!ignoreCase) {
    return searchToObj(search)[key];
  }
  key = key.toLocaleLowerCase();
  const obj = searchToObj(search);
  if (obj[key]) {
    return obj[key];
  }
  let result = null;
  Object.entries(obj).some(([name, value]) => {
    if (name.toLocaleLowerCase() === key) {
      result = value;
      return true;
    }
    return false;
  });
  return result;
};

export const getHashKey = (key, hash) => {
  if (typeof key !== "string") {
    return null;
  }
  if (typeof hash !== "string") {
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

const regReferenceHash = /^#(.+)$/;
const regPreviewReference = /^(((.+)\/(.+)\/(.+)\/(.+))\/(.+))$/;

export const authenticate = () => {
  const reference = regReferenceHash.test(window.location.hash)?window.location.hash.match(regReferenceHash)[1]:null;
  const state1 = regPreviewReference.test(reference)?null:searchToObj(window.location.search);
  const state2 = reference?{instanceReference: reference}:null;
  const state = {...state1, ...state2};
  const stateKey = btoa(JSON.stringify(state));
  const nonceKey = generateKey();
  //window.location.href = API.endpoints.auth(stateKey, nonceKey);
}