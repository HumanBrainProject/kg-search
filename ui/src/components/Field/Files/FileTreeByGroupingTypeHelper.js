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

const JSONPathRegex = /^(.+)\[(\d+)\]$/;
export const JSONPath = (data, path) => {
  if (!path) {
    return data;
  }
  if (typeof path == "string") {
    return JSONPath(data, path.split("."));
  }
  if (Array.isArray(path) && path.length) {
    if (typeof data === "object") {
      const [current, ...next] = path;
      if (JSONPathRegex.test(current)) {
        const [, prop, index] = current.match(JSONPathRegex);
        const list = data[prop];
        const number = parseInt(index);
        if (!Array.isArray(list) || isNaN(number) || number < 0 || number >= data.length) {
          return undefined;
        }
        return JSONPath(list[number], next);
      } else {
        return JSONPath(data[current], next);
      }
    }
    return undefined;
  }
  return data;
};

export const getTreeByGroupingType = (files, nameFieldPath, urlFieldPath, groupingType) => {
  if(!Array.isArray(files)) {
    files = [files]; // To be checked with the new indexer
  }
  const filesByFileBundles = {};
  files.forEach(file => {
    if (Array.isArray(file.groupingTypes) && file.groupingTypes.length) {
      file.groupingTypes
        .filter(type => !groupingType || type.name === groupingType)
        .forEach(type => {
          if (Array.isArray(type.fileBundles) && type.fileBundles.length) {
            type.fileBundles.forEach(fileBundle => {
              if (typeof fileBundle === "object" && fileBundle.reference) {
                if (!filesByFileBundles[fileBundle.reference]) {
                  filesByFileBundles[fileBundle.reference] = {
                    name: fileBundle.value,
                    reference: fileBundle.reference,
                    files: []
                  };
                }
                filesByFileBundles[fileBundle.reference].files.push(file);
              }
            });
          }
        });
    }
  });
  const tree = {
    name: groupingType,
    //url: `https://data.kg.ebrains.eu/zip?container=${groupingType}`,
    type: "folder",
    toggled: true,
    active: true
  };
  tree.children = Object.values(filesByFileBundles)
    .sort((fileBundleA, fileBundleB) => fileBundleA.name.localeCompare(fileBundleB.name))
    .map((fileBundle) => {
      const children = fileBundle.files
        .map(file => ({
          name: JSONPath(file, nameFieldPath),
          url: JSONPath(file, urlFieldPath),
          type: "file",
          thumbnail: file.thumbnailUrl && file.thumbnailUrl.url, //"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000041_VervetMonkey_3D-PLI_CoroSagiSec_dev/VervetThumbnail.jpg"
          data: file
        }))
        .sort((a, b) => a.name.localeCompare(b.name));
      return {
        name: fileBundle.name,
        //url: `${groupingType}/${fileBundle}`,
        type: "fileBundle",
        reference: fileBundle.reference,
        children: children
      };
    });
  return tree;
};
