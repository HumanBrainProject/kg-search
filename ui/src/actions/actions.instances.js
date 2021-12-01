/* eslint-disable no-unused-vars */
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

import * as types from "./actions.types";
import API from "../services/API";
import { clearGroupError } from "./actions.groups";
import { sessionFailure, logout } from "./actions";
import { history, store } from "../store";
import { getSearchKey } from "../helpers/BrowserHelpers";
import * as Sentry from "@sentry/browser";

export const loadInstanceRequest = () => {
  return {
    type: types.LOAD_INSTANCE_REQUEST
  };
};

export const loadInstanceSuccess = data => {
  return {
    type: types.LOAD_INSTANCE_SUCCESS,
    data: data
  };
};

export const loadInstanceNoData = error => {
  return {
    type: types.LOAD_INSTANCE_NO_DATA,
    error: error
  };
};

export const loadInstanceFailure = error => {
  return {
    type: types.LOAD_INSTANCE_FAILURE,
    error: error
  };
};

export const clearInstanceError = () => {
  return {
    type: types.CLEAR_INSTANCE_ERROR
  };
};


export const setInstance = data => {
  return {
    type: types.SET_INSTANCE,
    data: data
  };
};

export const cancelInstanceLoading = () => {
  return {
    type: types.CANCEL_INSTANCE_LOADING
  };
};

export const setPreviousInstance = () => {
  return {
    type: types.SET_PREVIOUS_INSTANCE
  };
};

export const clearAllInstances = () => {
  return {
    type: types.CLEAR_ALL_INSTANCES
  };
};

export const goBackToInstance = id => {
  return {
    type: types.GO_BACK_TO_INSTANCE,
    id: id
  };
};

export const updateLocation = () => {
  const state = store.getState();
  const id = state.instances.currentInstance?._id;
  if(id) {
    history.push(`/${window.location.search}#${id}`);
  } else {
    history.push(`/${window.location.search}`);
  }
  return {
    type: types.UPDATE_LOCATION
  };
};

export const goToSearch = (group, defaultGroup) => {
  return dispatch => {
    if (!group) {
      dispatch(clearGroupError());
      dispatch(logout());
    }
    dispatch(clearInstanceError());
    dispatch(clearAllInstances());
    history.replace(`/${(group && group !== defaultGroup)?("?group=" + group):""}`);
  };
};

// const instance = {
//   "_source": {
//     "type": {
//       "value": "Dataset"
//     },
//     "id": "4840dd00-058b-437c-9d0f-091b482d51b8",
//     "identifier": [
//       "4840dd00-058b-437c-9d0f-091b482d51b8",
//       "4840dd00-058b-437c-9d0f-091b482d51b8"
//     ],
//     "version": null,
//     "allVersionRef": null,
//     "versions": null,
//     "title": {
//       "value": "Afferents of the perirhinal cortex (PRH) in mice"
//     },
//     "editorId": null,
//     "contributors": [
//       {
//         "reference": "3e41be67-c4c8-4527-89d4-1951b9f647a0",
//         "value": "Schlegel, U."
//       },
//       {
//         "reference": "6e3405f4-1281-42b8-a32c-4d23cb7d8682",
//         "value": "Hvoslef-Eide, M."
//       },
//       {
//         "reference": "c3d292cd-f48f-4d89-9f44-acbc46b2ac18",
//         "value": "Lensjø, K."
//       },
//       {
//         "reference": "e43de45a-a8c5-42f9-87cb-a135ee4f9d21",
//         "value": "Fyhn, M."
//       }
//     ],
//     "doi": {
//       "value": "10.25493/GA16-M2P"
//     },
//     "ethicsAssessment": {
//       "value": "EU-compliant"
//     },
//     "projects": [
//       {
//         "reference": "c37cc15d-94c8-42f8-b548-3a4f06ff022b",
//         "value": "Connectivity of the perirhinal cortex in C57BL/J6 mice"
//       }
//     ],
//     "custodians": [
//       {
//         "reference": "3e41be67-c4c8-4527-89d4-1951b9f647a0",
//         "value": "Schlegel, U."
//       }
//     ],
//     "description": {
//       "value": "This dataset contains confocal image data of coronal sections following retrograde tract tracer injections (Cholera toxin (subunit b) conjugated with Alexa 594) in the perirhinal cortex in five adult male C57BL/J6 mice. \nThe images show connections from other areas in the brain to the perirhinal cortex."
//     },
//     "newInThisVersion": {
//       "value": "This is the first version of this research product."
//     },
//     "previewObjects": null,
//     "atlas": null,
//     "region": null,
//     "studiedBrainRegion": [
//       {
//         "reference": "2e95e582-74ac-46cf-b549-1a1764afe77c",
//         "value": "perirhinal area"
//       },
//       {
//         "reference": "328e3a78-c12f-4fd2-b60f-7d19ab281c64",
//         "value": "ectorhinal area"
//       }
//     ],
//     "preparation": null,
//     "modalityForFilter": null,
//     "experimentalApproach": [
//       {
//         "reference": "efa9154a-1bd3-49ed-8c96-e96744a6ba55",
//         "value": "anatomy"
//       },
//       {
//         "reference": "81524aee-0e15-4ca3-9ae7-03a88eb54d27",
//         "value": "cell population characterization"
//       },
//       {
//         "reference": "2ba8de18-a092-4e5e-82af-394cf32fe399",
//         "value": "microscopy"
//       },
//       {
//         "reference": "4ccfa2b8-fe75-4a17-98b7-e01b922c8f03",
//         "value": "histology"
//       },
//       {
//         "reference": "5fd69e3e-fc49-4e53-8d31-2c84d82c1db8",
//         "value": "cell population imaging"
//       },
//       {
//         "reference": "a9f37143-8fc7-42ca-88dd-b5d4a2026d23",
//         "value": "neural connectivity"
//       }
//     ],
//     "technique": [
//       {
//         "reference": "58f48481-e8ce-4447-a35e-42bb5d92d9b3",
//         "value": "confocal microscopy"
//       },
//       {
//         "reference": "4ae09ff3-1794-427c-a30c-147289a77e71",
//         "value": "immunohistochemistry"
//       },
//       {
//         "reference": "a2bb7aa2-b1b7-47f1-9ca6-be03779458e9",
//         "value": "spatial registration"
//       },
//       {
//         "reference": "96950b53-a4d6-4c5f-8902-7ea9896a1b9c",
//         "value": "retrograde tracing"
//       },
//       {
//         "reference": "3cbbe5a3-09c0-4aee-bb43-9c467b6d7438",
//         "value": "primary antibody staining"
//       },
//       {
//         "reference": "68cc1329-318b-4815-90e9-7a8a510f225e",
//         "value": "secondary antibody staining"
//       }
//     ],
//     "speciesFilter": null,
//     "dataAccessibility": {
//       "value": "under embargo"
//     },
//     "dataDescriptor": null,
//     "citation": {
//       "value": "Schlegel, U., Hvoslef-Eide, M., Lensjø, K., &amp; Fyhn, M. (2019) Afferents of the perirhinal cortex (PRH) in mice.\n[DOI: 10.25493/GA16-M2P]\n[DOI: 10.25493/GA16-M2P]: https://doi.org/10.25493/GA16-M2P"
//     },
//     "embargoRestrictedAccess": {
//       "value": "This dataset is temporarily under embargo. The data will become available for download after the embargo period.<br/><br/>If you are an authenticated user, <a href=\"https://kg.ebrains.eu/files/cscs/list?url=https://object.cscs.ch/v1/AUTH_4791e0a3b3de43e2840fe46d9dc2b334/ext-d000005_CortAfferents-PRH_Connectivity_pub\" target=\"_blank\"> you should be able to access the data here</a>"
//     },
//     "embargo": null,
//     "filesOld": null,
//     "filesAsyncUrl": null,
//     "publications": null,
//     "methods": null,
//     "keywords": [
//       {
//         "value": "Coronal Sections"
//       },
//       {
//         "value": "Retrograde Tracer"
//       }
//     ],
//     "viewer": null,
//     "subjectGroupOrSingleSubjectOld": null,
//     "subjectGroupOrSingleSubject": [
//       {
//         "children": {
//           "children": [
//             {
//               "children": null,
//               "collapsible": false,
//               "subject_name": {
//                 "reference": "561fa693-6341-4767-9164-66aa3c40153f",
//                 "value": "G07"
//               },
//               "numberOfSubjects": null,
//               "species": [
//                 {
//                   "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//                   "value": "Mus musculus"
//                 }
//               ],
//               "sex": [
//                 {
//                   "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//                   "value": "male"
//                 }
//               ],
//               "strain": null,
//               "age": null,
//               "ageCategory": null,
//               "weight": null
//             },
//             {
//               "children": null,
//               "collapsible": false,
//               "subject_name": {
//                 "reference": "a9007160-7476-49e6-84d8-18b771e067fe",
//                 "value": "G05"
//               },
//               "numberOfSubjects": null,
//               "species": [
//                 {
//                   "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//                   "value": "Mus musculus"
//                 }
//               ],
//               "sex": [
//                 {
//                   "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//                   "value": "male"
//                 }
//               ],
//               "strain": null,
//               "age": null,
//               "ageCategory": null,
//               "weight": null
//             },
//             {
//               "children": null,
//               "collapsible": false,
//               "subject_name": {
//                 "reference": "b074e51c-2565-4f11-8a10-a7d890c629e7",
//                 "value": "G08"
//               },
//               "numberOfSubjects": null,
//               "species": [
//                 {
//                   "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//                   "value": "Mus musculus"
//                 }
//               ],
//               "sex": [
//                 {
//                   "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//                   "value": "male"
//                 }
//               ],
//               "strain": null,
//               "age": null,
//               "ageCategory": null,
//               "weight": null
//             },
//             {
//               "children": null,
//               "collapsible": false,
//               "subject_name": {
//                 "reference": "98c673f4-feba-4e3c-885e-5efe188c0848",
//                 "value": "G06"
//               },
//               "numberOfSubjects": null,
//               "species": [
//                 {
//                   "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//                   "value": "Mus musculus"
//                 }
//               ],
//               "sex": [
//                 {
//                   "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//                   "value": "male"
//                 }
//               ],
//               "strain": null,
//               "age": null,
//               "ageCategory": null,
//               "weight": null
//             },
//             {
//               "children": [
//                 {
//                   "subject_name": {
//                     "reference": null,
//                     "value": "State 1"
//                   },
//                   "age": {
//                     "value": "16 - 18"
//                   },
//                   "ageCategory": [
//                     {
//                       "reference": "584611b0-3c4b-418f-8617-127adafb0cea",
//                       "value": "adult"
//                     }
//                   ],
//                   "weight": {
//                     "value": "27 gram"
//                   }
//                 },
//                 {
//                   "subject_name": {
//                     "reference": null,
//                     "value": "State 2"
//                   },
//                   "age": {
//                     "value": "12 centimeter"
//                   },
//                   "ageCategory": [
//                     {
//                       "reference": "89e4eae2-acea-4cd9-bfb3-19eb108b05ff",
//                       "value": "infant"
//                     }
//                   ],
//                   "weight": null
//                 }
//               ],
//               "collapsible": false,
//               "subject_name": {
//                 "reference": "1e245174-c874-414d-b1e4-ddd1c74a9d21",
//                 "value": "G04"
//               },
//               "numberOfSubjects": null,
//               "species": [
//                 {
//                   "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//                   "value": "Mus musculus"
//                 }
//               ],
//               "sex": [
//                 {
//                   "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//                   "value": "male"
//                 }
//               ],
//               "strain": null,
//               "age": null,
//               "ageCategory": null,
//               "weight": null
//             }
//           ],
//           "collapsible": true,
//           "subject_name": {
//             "reference": "97eb9efd-8681-4487-a067-f6f51c541e02",
//             "value": "sub. gr. w sub & states"
//           },
//           "numberOfSubjects": {
//             "value": "5"
//           },
//           "species": [
//             {
//               "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//               "value": "Mus musculus"
//             }
//           ],
//           "sex": [
//             {
//               "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//               "value": "male"
//             }
//           ],
//           "strain": null,
//           "age": null,
//           "ageCategory": null,
//           "weight": null
//         }
//       },
//       {
//         "children": {
//           "subject_name": {
//             "reference": "97eb9efd-8681-4487-a067-f6f51c541e02",
//             "value": "this is a subject"
//           },
//           "numberOfSubjects": {
//             "value": "5"
//           },
//           "species": [
//             {
//               "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//               "value": "Mus musculus"
//             }
//           ],
//           "sex": [
//             {
//               "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//               "value": "male"
//             }
//           ],
//           "strain": null,
//           "age": null,
//           "ageCategory": null,
//           "weight": null
//         }
//       },
//       {
//         "children": {
//           "children": [
//             {
//               "subject_name": {
//                 "reference": null,
//                 "value": "State 1"
//               },
//               "age": {
//                 "value": "16 - 18"
//               },
//               "ageCategory": [
//                 {
//                   "reference": "584611b0-3c4b-418f-8617-127adafb0cea",
//                   "value": "adult"
//                 }
//               ],
//               "weight": {
//                 "value": "27 gram"
//               }
//             },
//             {
//               "subject_name": {
//                 "reference": null,
//                 "value": "State 2"
//               },
//               "age": {
//                 "value": "12 centimeter"
//               },
//               "ageCategory": [
//                 {
//                   "reference": "89e4eae2-acea-4cd9-bfb3-19eb108b05ff",
//                   "value": "infant"
//                 }
//               ],
//               "weight": null
//             }
//           ],
//           "subject_name": {
//             "reference": "97eb9efd-8681-4487-a067-f6f51c541e02",
//             "value": "this is a subject with states"
//           },
//           "numberOfSubjects": {
//             "value": "5"
//           },
//           "species": [
//             {
//               "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//               "value": "Mus musculus"
//             }
//           ],
//           "sex": [
//             {
//               "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//               "value": "male"
//             }
//           ],
//           "strain": null,
//           "age": null,
//           "ageCategory": null,
//           "weight": null
//         }
//       },
//       {
//         "children": {
//           "collapsible": false,
//           "children": [
//             {
//               "subject_name": {
//                 "reference": null,
//                 "value": "State 1"
//               },
//               "age": {
//                 "value": "16 - 18"
//               },
//               "ageCategory": [
//                 {
//                   "reference": "584611b0-3c4b-418f-8617-127adafb0cea",
//                   "value": "adult"
//                 }
//               ],
//               "weight": {
//                 "value": "27 gram"
//               }
//             },
//             {
//               "subject_name": {
//                 "reference": null,
//                 "value": "State 2"
//               },
//               "age": {
//                 "value": "12 centimeter"
//               },
//               "ageCategory": [
//                 {
//                   "reference": "89e4eae2-acea-4cd9-bfb3-19eb108b05ff",
//                   "value": "infant"
//                 }
//               ],
//               "weight": null
//             }
//           ],
//           "subject_name": {
//             "reference": "97eb9efd-8681-4487-a067-f6f51c541e02",
//             "value": "this is a subject group with states"
//           },
//           "numberOfSubjects": {
//             "value": "5"
//           },
//           "species": [
//             {
//               "reference": "d9875ebd-260e-4337-a637-b62fed4aa91d",
//               "value": "Mus musculus"
//             }
//           ],
//           "sex": [
//             {
//               "reference": "744c9204-4aea-4eff-a4f4-d79f008b355f",
//               "value": "male"
//             }
//           ],
//           "strain": null,
//           "age": null,
//           "ageCategory": null,
//           "weight": null
//         }
//       }
//     ],
//     "tissueSamples": null,
//     "searchableInstance": true,
//     "first_release": null,
//     "last_release": null,
//     "license_info": {
//       "url": "https://creativecommons.org/licenses/by/4.0/legalcode",
//       "value": "Creative Commons Attribution 4.0 International"
//     },
//     "external_datalink": null
//   }
// };

export const loadInstance = (group, id, shouldUpdateLocation=false) => {
  return dispatch => {
    dispatch(loadInstanceRequest());
    API.axios
      .get(API.endpoints.instance(group, id))
      .then(response => {
        dispatch(loadInstanceSuccess(response.data));
        // dispatch(loadInstanceSuccess(instance));
        if(shouldUpdateLocation) {
          dispatch(updateLocation());
        }
      })
      .catch(e => {
        const { response } = e;
        const { status } = response;
        switch (status) {
        case 400: // Bad Request
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadInstanceFailure(error));
          break;
        }
        case 401: // Unauthorized
        case 403: // Forbidden
        case 511: // Network Authentication Required
        {
          const error = "Your session has expired. Please login again.";
          dispatch(sessionFailure(error));
          break;
        }
        case 500:
        {
          Sentry.captureException(e);
          break;
        }
        case 404:
        {
          const url = `${window.location.protocol}//${window.location.host}${window.location.pathname}?group=curated`;
          const link = `<a href=${url}>${url}</a>`;
          const group = getSearchKey("group");
          const error = (group && group === "curated")? "The page you requested was not found." :
            `The page you requested was not found. It might not yet be public and authorized users might have access to it in the ${link} or in in-progress view`;
          dispatch(loadInstanceFailure(error));
          break;
        }
        default:
        {
          const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
          dispatch(loadInstanceFailure(error));
        }
        }
      });
  };
};

export const loadPreview = id => {
  return dispatch => {
    dispatch(loadInstanceRequest());
    API.axios
      .get(API.endpoints.preview(id))
      .then(response => {
        if (response.data && !response.data.error) {
          response.data._id = id;
          dispatch(loadInstanceSuccess(response.data));
        } else if (response.data && response.data.error) {
          dispatch(loadInstanceFailure(response.data.message ? response.data.message : response.data.error));
        } else {
          const error = `The instance with id ${id} is not available.`;
          dispatch(loadInstanceNoData(error));
        }
      })
      .catch(e => {
        if (e.stack === "SyntaxError: Unexpected end of JSON input" || e.message === "Unexpected end of JSON input") {
          dispatch(loadInstanceNoData(e));
        } else {
          const { response } = e;
          const { status } = response;
          switch (status) {
          case 401: // Unauthorized
          case 403: // Forbidden
          case 511: // Network Authentication Required
          {
            const error = "Your session has expired. Please login again.";
            dispatch(sessionFailure(error));
            break;
          }
          case 500:
          {
            Sentry.captureException(e.message);
            break;
          }
          case 404:
          default:
          {
            const error = `The service is temporarily unavailable. Please retry in a few minutes. (${e.message?e.message:e})`;
            dispatch(loadInstanceFailure(error));
          }
          }
        }
      });
  };
};