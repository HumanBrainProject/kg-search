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

export const getTreeByGroupingType = (files, nameField, urlField, fileMapping, groupingType) => {
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
              if (!filesByFileBundles[fileBundle]) {
                filesByFileBundles[fileBundle] = [];
              }
              filesByFileBundles[fileBundle].push(file);
            });
          }
        });
    }
  });
  const tree = {
    name: groupingType,
    //url: `/proxy/export?container=${groupingType}`,
    type: "folder",
    toggled: true,
    active: true
  };
  tree.children = Object.entries(filesByFileBundles)
    .sort(([fileBundleA], [fileBundleB]) => fileBundleA.localeCompare(fileBundleB))
    .map(([fileBundle, files]) => {
      const children = files
        .map(file => ({
          name: file[nameField],
          url: file[urlField],
          type: "file",
          size: file.fileSize, // v1
          thumbnail: file.thumbnailUrl && file.thumbnailUrl.url, //"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000041_VervetMonkey_3D-PLI_CoroSagiSec_dev/VervetThumbnail.jpg"
          details: fileMapping?{data: file, mapping: fileMapping}:null
        }))
        .sort((a, b) => a.name.localeCompare(b.name));
      const folder = {
        name: fileBundle,
        //url: `${groupingType}/${fileBundle}`,
        type: "folder",
        children: children
      };
      return folder;
    });
  return tree;
};
