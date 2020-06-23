import React, {PureComponent} from "react";
import ReactPiwik from "react-piwik";
import { Treebeard } from "react-treebeard";
import { Text } from "../../Text/Text";
import { termsOfUse } from "../../../data/termsOfUse.js";
import "./HierarchicalFiles.css";
import theme from "./Theme";

export default class HierarchicalFiles extends PureComponent {
  constructor(props){
    super(props);
    this.state = {
      cursor: null,
      data: {},
      valueUrlMap: {},
      showTermsOfUse: false
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

  constructResult = (resultObj, val) => {
    if(val.type !== "file") {
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
            this.constructResult(child, value);
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
      this.constructResult(result, pathObj);
    }
    this.setState({data: {...result} });
  }

  onToggle = (node, toggled) => {
    const {cursor, data} = this.state;
    if (cursor) {
      this.setState({cursor: {active: false}});
    }
    if(node.url) {
      node.active = true;
    }
    if (node.children) {
      node.toggled = toggled;
    }
    this.setState({cursor: node, data: {...data} });
  }

  toggleTermsOfUse = () => this.setState(state => ({showTermsOfUse: !state.showTermsOfUse}));

  handleClick = url => e => {
    e.stopPropagation();
    ReactPiwik.push(["trackLink", url, "download"]);
  }

  render() {
    const {show} = this.props;
    if(!show) {
      return null;
    }
    const {cursor, data, valueUrlMap, showTermsOfUse} = this.state;
    const name = cursor && cursor.url && valueUrlMap[cursor.url].value;
    const fileSize = cursor && cursor.url && valueUrlMap[cursor.url].fileSize;
    return (
      <div className="kgs-hierarchical-files">
        <Treebeard
          data={data}
          onToggle={this.onToggle}
          style={{...theme}}
        />
        {name &&
        <div className="kgs-hierarchical-files__details">
          <div><i className="fa fa-5x fa-file-o"></i></div>
          <div className="kgs-hierarchical-files__info">
            <div>
              <div><strong>Name:</strong> {name}</div>
              <div><strong>Size:</strong> {fileSize}</div>
              <a type="button" className="btn kgs-hierarchical-files__info_link" href={cursor.url} onClick={this.handleClick(cursor.url)}><i className="fa fa-download"></i> Download file</a>
              <div className="kgs-hierarchical-files__info_agreement"><span>By downloading the file you agree to the <a href onClick={this.toggleTermsOfUse}>Terms of use</a></span></div>
            </div>
          </div>
          {showTermsOfUse &&
            <div className="kgs-hierarchical-files__info_terms_of_use">
              <Text content={termsOfUse} isMarkdown={true} />
            </div>
          }
        </div>}
      </div>
    );
  }
}
