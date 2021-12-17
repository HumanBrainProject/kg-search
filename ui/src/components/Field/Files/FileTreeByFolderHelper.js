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

import { JSONPath } from "./FileTreeByGroupingTypeHelper";

const buildTreeStructureForFile = (rootNode, file, nbOfPathToSkip, rootUrlSeparator, urlFieldPath) => {
  const fileUrl = JSONPath(file, urlFieldPath);
  if (fileUrl && typeof fileUrl === "string") {
    const path = fileUrl.split("/").slice(nbOfPathToSkip);
    let node = rootNode;
    path.forEach((name, index) => {
      if(index === (path.length - 1)) { // file
        node.paths[name] = {
          name: name,
          url: fileUrl,
          type: "file",
          thumbnail: file.thumbnailUrl && file.thumbnailUrl.url, //"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000041_VervetMonkey_3D-PLI_CoroSagiSec_dev/VervetThumbnail.jpg"
          data: file
        };
      } else { // folder
        if(!node.paths[name]) { // is not already created
          node.paths[name] = {
            name: name,
            url: `${node.url}${node === rootNode?rootUrlSeparator:"/"}${name}`,
            type: "folder",
            paths: {}
          };
        }
        node = node.paths[name];
      }
    });
  }
};

const setChildren = node => {
  if(node.type === "folder") {
    node.children = [];
    const paths = Object.values(node.paths);
    if(!paths.every(el => el.type === "folder") && !paths.every(el => el.type === "file")) {
      paths.sort((a, b) => b.type.toLowerCase().localeCompare(a.type.toLowerCase()));
    }
    delete node.paths;
    paths.forEach(child => {
      node.children.push(child);
      if(child.type === "folder") {
        setChildren(child);
      }
    });
  }
};

const getPath = url => {
  if (!url) {
    return [];
  }
  const segments = url.split("/");
  return segments.slice(0, segments.length-1);
};

const getCommonPath = (files, urlFieldPath) => {
  const urls = files.map(file => JSONPath(file, urlFieldPath)).sort();
  const firstFilePath = getPath(urls[0]);
  const lastFilePath = getPath(urls.pop());
  const max = firstFilePath.length > lastFilePath.length?lastFilePath.length:firstFilePath.length;
  let index = 0;
  while(index<max && firstFilePath[index] === lastFilePath[index]) {
    index++;
  }
  return firstFilePath.splice(0, index);
};

export const getTreeByFolder = (files, urlFieldPath) => {
  if(!Array.isArray(files)) {
    files = [files]; // To be checked with the new indexer
  }
  const commonPath = getCommonPath(files, urlFieldPath);
  const rootPathIndex = 6;
  const url = commonPath.length<=rootPathIndex?commonPath.join("/"):`${commonPath.slice(0,rootPathIndex).join("/")}?prefix=${commonPath.slice(rootPathIndex).join("/")}`;
  const tree = {
    name: commonPath[commonPath.length-1],
    url: `/proxy/export?container=${url}`,
    isRootNode: true,
    type: "folder",
    paths: {},
    toggled: true,
    active: true
  };
  const nbOfPathToSkip = commonPath.length;
  const rootUrlSeparator = nbOfPathToSkip>rootPathIndex?"/":"?prefix=";
  files.forEach(file => buildTreeStructureForFile(tree, file, nbOfPathToSkip, rootUrlSeparator, urlFieldPath));
  setChildren(tree);
  return tree;
};
