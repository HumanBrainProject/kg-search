import React, {PureComponent} from "react";
import { Treebeard } from "react-treebeard";
import { Text } from "../../Text/Text";
import { termsOfUse } from "../../../data/termsOfUse.js";
import "./HierarchicalFiles.css";
import theme from "./Theme";
import Link from "../../Link/Link";

export default class HierarchicalFiles extends PureComponent {
  constructor(props){
    super(props);
    this.state = {
      curson: false,
      data: {},
      valueUrlMap: {}
    };
  }

  componentDidMount() {
    const {show} = this.props;
    if(show) {
      this.constructValueUrlMap();
      this.constructData();
    }
  }

  buildObjectStructure = (obj, path, url) => path.forEach((d, index) => {
    if(index === (path.length - 1)) {
      obj[d] = {
        url: url,
        type: "file"
      };
    }
    obj = obj[d] || (obj[d] = {});
    return obj;
  });

  constructValueUrlMap = () => {
    const valUrlMap = this.props.data.reduce((acc, currentVal) => {
      acc[currentVal.url] = {
        value: currentVal.value,
        fileSize: currentVal.fileSize
      };
      return acc;
    }, {});
    this.setState({valueUrlMap: {...valUrlMap}});
  }

  constructResult = (resultObj, val, isRootLevel) => {
    if(val.type !== "file" || isRootLevel) {
      Object.entries(val).forEach(([key, value]) => {
        if(value.type === "file") {
          resultObj.children.push({
            name: key,
            url: value.url
          });
        } else {
          resultObj.children.push({
            name: key,
            children: []
          });
        }
        resultObj.children.forEach(child => {
          if(child.name === key){
            this.constructResult(child, value, false);
          }
        });
      });
    }
  }

  findCommonPath = array => {
    const sortedArray = array.sort();
    const firstElement = sortedArray[0];
    const lastElement= sortedArray.pop();
    const firstElementLength = firstElement.length;
    let index= 0;
    while(index<firstElementLength && firstElement.charAt(index) === lastElement.charAt(index)) {
      index++;
    }
    return firstElement.substring(0, index);
  }

  constructData = () => {
    const urlsToFindCommon = this.props.data.map(u => u.url);
    const commonPath = this.findCommonPath(urlsToFindCommon).split("/");
    const result = {
      name: commonPath[commonPath.length-2]
    };
    const pathObj = {};
    this.props.data.forEach(item => {
      const path = item.url.split("/").slice(commonPath.length-1);
      this.buildObjectStructure(pathObj, path, item.url);
    });
    if (pathObj) {
      result.children = [];
      result.toggled = true;
      this.constructResult(result, pathObj, true);
    }
    this.setState({data: {...result} });
  }

  onToggle = (node, toggled) => {
    const {cursor, data} = this.state;
    if (cursor) {
      this.setState({cursor: {active: false}});
    }
    node.active = true;
    if (node.children) {
      node.toggled = toggled;
    }
    this.setState({cursor: node, data: {...data} });
  }

  render(){
    const {show} = this.props;
    if(!show) {
      return null;
    }
    const {cursor, data, valueUrlMap} = this.state;
    const name = cursor && cursor.url && valueUrlMap[cursor.url]["value"];
    const fileSize = cursor && cursor.url && valueUrlMap[cursor.url]["fileSize"];
    return (
      <div className="kgs-hierarchical-files">
        <Treebeard
          data={data}
          onToggle={this.onToggle}
          style={{...theme}}
        />
        {name &&
        <div className="kgs-hierarchical-files__details">
          <div>
            <i className="fa fa-2x fa-file-o"></i>
            <Link url={cursor.url} label={name}  isAFileLink={true} />
            <strong> ({fileSize})</strong>
          </div>
          <Text content={termsOfUse} isMarkdown={true} />
        </div>}
      </div>
    );
  }
}
