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

import React, { useState } from "react";
import ReactPiwik from "react-piwik";
import { Treebeard, decorators } from "react-treebeard";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

//import { Text } from "../../Text/Text";
import { InfoPanel } from "../../InfoPanel/InfoPanel";
import File from "./File";

import { termsOfUse } from "../../../data/termsOfUse.js";
import "./HierarchicalFiles.css";
import theme from "./Theme";
import Header from "./Header";

const buildTreeStructureForFile = (tree, file, nbOfPathToSkip, rootUrlSeparator, urlField, fileMapping) => {
  const path = file[urlField].split("/").slice(nbOfPathToSkip);
  let node = tree;
  path.forEach((name, index) => {
    if(index === (path.length - 1)) { // file
      node.paths[name] = {
        name: name,
        url: file[urlField],
        type: "file",
        size: file.fileSize, // v1
        thumbnail: file.thumbnailUrl && file.thumbnailUrl.url, //"https://object.cscs.ch/v1/AUTH_227176556f3c4bb38df9feea4b91200c/hbp-d000041_VervetMonkey_3D-PLI_CoroSagiSec_dev/VervetThumbnail.jpg"
        details: fileMapping?{data: file, mapping: fileMapping}:null
      };
    } else { // folder
      if(!node.paths[name]) { // is not already created
        node.paths[name] = {
          name: name,
          url: `${node[urlField]}${node === tree?rootUrlSeparator:"/"}${name}`,
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

const getCommonPath = (files, key) => {
  const urls = files.map(file => file[key]).sort();
  const firstFilePath = getPath(urls[0]);
  const lastFilePath = getPath(urls.pop());
  const max = firstFilePath.length > lastFilePath.length?lastFilePath.length:firstFilePath.length;
  let index = 0;
  while(index<max && firstFilePath[index] === lastFilePath[index]) {
    index++;
  }
  return firstFilePath.splice(0, index);
};

const getTree = (files, urlField, fileMapping) => {
  if(!Array.isArray(files)) {
    files = [files]; // To be checked with the new indexer
  }
  const commonPath = getCommonPath(files, urlField);
  const rootPathIndex = 6;
  const url = commonPath.length<=rootPathIndex?commonPath.join("/"):`${commonPath.slice(0,rootPathIndex).join("/")}?prefix=${commonPath.slice(rootPathIndex).join("/")}`;
  const tree = {
    name: commonPath[commonPath.length-1],
    url: `/proxy/export?container=${url}`,
    type: "folder",
    paths: {},
    toggled: true,
    active: true
  };
  const nbOfPathToSkip = commonPath.length;
  const rootUrlSeparator = nbOfPathToSkip>rootPathIndex?"/":"?prefix=";
  files.forEach(file => buildTreeStructureForFile(tree, file, nbOfPathToSkip, rootUrlSeparator, urlField, fileMapping));
  setChildren(tree);
  return tree;
};

const Download = ({name, type, url}) => {

  const [showTermsOfUse, toggleTermsOfUse] = useState(false);

  const trackDownload = e => {
    e.stopPropagation();
    ReactPiwik.push(["trackLink", url, "download"]);
  };

  const openTermsOfUse = e => {
    e && e.preventDefault();
    toggleTermsOfUse(true);
  };

  const closeTermsOfUse = e => {
    e && e.preventDefault();
    toggleTermsOfUse(false);
  };

  return (
    <>
      <a type="button" className="btn kgs-hierarchical-files__info_link" rel="noopener noreferrer" target="_blank" href={url} onClick={trackDownload} >
        <FontAwesomeIcon icon="download" /> {name}
      </a>
      <div className="kgs-hierarchical-files__info_agreement"><span>By downloading the {type} you agree to the <button onClick={openTermsOfUse}><strong>Terms of use</strong></button></span></div>
      {showTermsOfUse && (
        <InfoPanel text={termsOfUse} onClose={closeTermsOfUse} />
      )}
    </>
  );
};

const Node = ({node, isRootNode, group, enableDownload}) => {
  return (
    <div className="kgs-hierarchical-files__details">
      <div>{node.thumbnail ? <img height="80" src={node.thumbnail} alt={node.url} />:<FontAwesomeIcon icon={node.type} size="5x"/>}</div>
      <div className="kgs-hierarchical-files__info">
        <div>
          <div><strong>Name:</strong> {node.name}</div>
          {node.size  && <div><strong>Size:</strong> {node.size}</div>}
          {enableDownload && (
            <Download name={`Download ${isRootNode?"dataset":node.type}`} type={node.type} url={node.url} />
          )}
          {node.type === "file" && node.details && node.details.data && node.details.mapping && (
            <File data={node.details.data} mapping={node.details.mapping} group={group} />
          )}
        </div>
      </div>
    </div>
  );
};

class HierarchicalFiles extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      node: {},
      tree: {}
    };
  }

  componentDidMount() {
    const {data, urlField, fileMapping} = this.props;
    const tree = getTree(data, urlField, fileMapping);
    this.setState({tree: tree, node: tree });
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
    e && e.preventDefault();
    this.setState(state => ({showTermsOfUse: !state.showTermsOfUse}));
  };

  render() {
    return (
      <>
        <div className="kgs-hierarchical-files">
          <Treebeard
            data={this.state.tree}
            onToggle={this.onToggle}
            decorators={{...decorators, Header}}
            style={{...theme}}
          />
          {this.state.node.active && (
            <Node node={this.state.node} isRootNode={this.state.node === this.state.tree} group={this.props.group} enableDownload={this.props.allowFolderDownload || this.state.node.type === "file"} />
          )}
        </div>
      </>
    );
  }
}

export default HierarchicalFiles;
