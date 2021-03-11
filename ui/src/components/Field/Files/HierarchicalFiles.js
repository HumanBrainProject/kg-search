/*
*   Copyright (c) 2020, EPFL/Human Brain Project PCO
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

import React from "react";
import ReactPiwik from "react-piwik";
import { Treebeard, decorators } from "react-treebeard";
import { Text } from "../../Text/Text";
import { termsOfUse } from "../../../data/termsOfUse.js";
import "./HierarchicalFiles.css";
import theme from "./Theme";
import Header from "./Header";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const buildTreeStructureForFile = (tree, file, nbOfPathToSkip, rootUrlSeparator) => {
  const path = file.url.split("/").slice(nbOfPathToSkip);
  let node = tree;
  path.forEach((name, index) => {
    if(index === (path.length - 1)) { // file
      node.paths[name] = {
        name: name,
        url: file.url,
        type: "file",
        size: file.fileSize,
        thumbnail: file.thumbnailUrl && file.thumbnailUrl.url //"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000041_VervetMonkey_3D-PLI_CoroSagiSec_dev/VervetThumbnail.jpg"
      };
    } else { // folder
      if(!node.paths[name]) { // is not already created
        node.paths[name] = {
          name: name,
          url: `${node.url}${node === tree?rootUrlSeparator:"/"}${name}`,
          type: "folder",
          paths: {}
        };
      }
      node = node.paths[name];
    }
  });
};

const setChildren = node => {
  if(node.type === "folder") {
    node.children = [];
    const paths = Object.values(node.paths);
    if(!paths.every(el => el.type === "folder") && !paths.every(el => el.type === "file")) {
      paths.sort((a, b) => b.type.toLowerCase().localeCompare(a.type.toLowerCase()));
    }
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

const getCommonPath = files => {
  const urls = files.map(file => file.url).sort();
  const firstFilePath = getPath(urls[0]);
  const lastFilePath = getPath(urls.pop());
  const max = firstFilePath.length > lastFilePath.length?lastFilePath.length:firstFilePath.length;
  let index = 0;
  while(index<max && firstFilePath[index] === lastFilePath[index]) {
    index++;
  }
  return firstFilePath.splice(0, index);
};

const getTree = files => {
  if(!Array.isArray(files)) {
    files = [files]; // To be checked with the new indexer
  }
  const commonPath = getCommonPath(files);
  const rootPathIndex = 6;
  const url = commonPath.length<=rootPathIndex?commonPath.join("/"):`${commonPath.slice(0,rootPathIndex).join("/")}?prefix=${commonPath.slice(rootPathIndex).join("/")}`;
  const tree = {
    name: commonPath[commonPath.length-1],
    url: `/proxy/export?container=${url}`,
    type: "folder",
    paths: {},
    toggled: true
  };
  const nbOfPathToSkip = commonPath.length;
  const rootUrlSeparator = nbOfPathToSkip>rootPathIndex?"/":"?prefix=";
  files.forEach(file => buildTreeStructureForFile(tree, file, nbOfPathToSkip, rootUrlSeparator));
  setChildren(tree);
  return tree;
};

export default class HierarchicalFiles extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      node: {},
      tree: {},
      showTermsOfUse: false
    };
  }

  componentDidMount() {
    const {show, data} = this.props;
    if (show) {
      const tree = getTree(data);
      this.setState({tree: tree });
    }
  }

  onToggle = (node, toggled) => {
    node.active = true;
    if (node.children) {
      node.toggled = toggled;
    }
    if(node.url !== this.state.node.url) {
      const previousNode = this.state.node;
      previousNode.active = false;
    }
    this.setState({node: node});
  }

  toggleTermsOfUse = e => {
    e.preventDefault();
    this.setState(state => ({showTermsOfUse: !state.showTermsOfUse}));
  };

  handleClick = url => e => {
    e.stopPropagation();
    ReactPiwik.push(["trackLink", url, "download"]);
  }

  render() {
    const {show} = this.props;
    if(!show) {
      return null;
    }
    const {node, tree, showTermsOfUse} = this.state;
    const name = node && node.name;
    const size = node && node.size;
    const type = node && node.type;
    return (
      <>
        <div className="kgs-hierarchical-files">
          <Treebeard
            data={tree}
            onToggle={this.onToggle}
            decorators={{...decorators, Header}}
            style={{...theme}}
          />
          {node.active && (
            <div className="kgs-hierarchical-files__details">
              <div>{node.thumbnail ? <img height="80" src={node.thumbnail} alt={node.url} />:<FontAwesomeIcon icon={type} size="5x"/>}</div>
              <div className="kgs-hierarchical-files__info">
                <div>
                  <div><strong>Name:</strong> {name}</div>
                  {size  && <div><strong>Size:</strong> {size}</div>}
                  <a type="button" className="btn kgs-hierarchical-files__info_link" rel="noopener noreferrer" target="_blank" href={node.url} onClick={this.handleClick(node.url)} ><FontAwesomeIcon icon="download" /> Download {type}</a>
                  <div className="kgs-hierarchical-files__info_agreement"><span>By downloading the {type} you agree to the <button onClick={this.toggleTermsOfUse}><strong>Terms of use</strong></button></span></div>
                </div>
              </div>
            </div>
          )}
        </div>
        {showTermsOfUse &&
          <div className="kgs-hierarchical-files__info_terms_of_use">
            <Text content={termsOfUse} isMarkdown={true} />
          </div>}
      </>
    );
  }
}
