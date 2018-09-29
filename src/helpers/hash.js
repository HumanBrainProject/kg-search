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

const isNativeObject = obj => obj !== null && typeof obj === "object" && (!obj.constructor || (obj.constructor && (!obj.constructor.name || obj.constructor.name === "Object")));

export const hash = (obj) => {
  if (Array.isArray(obj)) {
    return "[" + obj.reduce((res, e) => {
      return res + (res === ""?"":",") + hash(e);
    }, "") + "]";
  } else if (typeof obj === "object") {
    if (isNativeObject(obj))  {
      return "{" + Object.entries(obj).reduce((res, [key, value]) => {
        if (key === "__proto__") {
          return res;
        }
        return res + (res === ""?"":",") + key + ":" + hash(value);
      }, "") + "}";
    }
    return "<object>";
  } else if (typeof obj === "function") {
    return "<function>";
  } else if (obj === null) {
    return "null";
  } else if (obj === undefined) {
    return "undefined";
  }
  return obj.toString();
};