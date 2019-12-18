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

// import * as types from "../actions.types";

// const initialState = {
//   message: null,
//   retry: null, // { label: "", action: null }
//   cancel: null // { label: "", action: null }
// };

// export function reducer(state = initialState, action = {}) {
//   switch (action.type) {
//   case types.LOAD_SEARCH_SERVICE_FAILURE: {
//     const serviceStatus = action.status?` [code ${action.status}]`:"";
//     let message = `The search engine is temporary unavailable${serviceStatus}. Please retry in a moment.`;
//     if (action.status === 404 && action.group) {
//       message = `The group of group "${action.group}" is temporary not available${serviceStatus}. Please retry in a moment.`;
//     }
//     return {
//       message: message,
//       retry: {
//         label: "Retry",
//         action: types.LOAD_SEARCH_REQUEST
//       },
//       cancel: {
//         label: "Cancel",
//         action: types.CANCEL_SEARCH
//       }
//     };
//   }
//   case types.LOAD_SEARCH_SESSION_FAILURE: {
//     const sessionStatus = action.status?` [code ${action.status}]`:"";
//     return {
//       message: `Your session has expired${sessionStatus}. Please login again.`,
//       retry: {
//         label: "Login",
//         action: types.AUTHENTICATE
//       }
//     };
//   }
//   }
// }