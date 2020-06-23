import React from "react";
import ReactPiwik from "react-piwik";
import { Treebeard } from "react-treebeard";
import { Text } from "../../Text/Text";
import { termsOfUse } from "../../../data/termsOfUse.js";
import "./HierarchicalFiles.css";
import theme from "./Theme";

export default class HierarchicalFiles extends React.Component {
  constructor(props){
    super(props);
    this.state = {
      node: {},
      data: {},
      showTermsOfUse: false
    };
  }

  componentDidMount() {
    const {show} = this.props;
    if(show) {
      this.constructData();
    }
  }

  buildObjectStructure = (obj, path, item) => {
    path.forEach((d, index) => {
      if(index === (path.length - 1)) {
        obj.children[d] = {
          url: item.url,
          type: "file",
          name: d,
          size: item.fileSize,
          thumbnail: item.thumbnailUrl && item.thumbnailUrl.url
        };
      } else {
        if(!obj.children[d]) {
          obj.children[d] = {
            children: {},
            type: "folder",
            url: `${obj.url}${d}`,
            name: d
          };
        }
        obj = obj.children[d];
      }
    });
  };

  constructResult = (resultObj, val) => {
    if(val.type === "folder") {
      Object.entries(val.children).forEach(([key, value]) => {
        if(value.type === "file") {
          resultObj.children.push({
            name: key,
            type: "file",
            url: value.url,
            size: value.size,
            thumbnail: value.thumbnail
          });
        } else {
          const child ={
            name: key,
            children: [],
            type: "folder",
            url: value.url
          };
          resultObj.children.push(child);
          this.constructResult(child, value);
        }
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
    const commonPath = this.findCommonPath(urlsToFindCommon);
    const pathArray = commonPath.split("/");
    const result = {
      name: pathArray[pathArray.length-2]
    };
    const pathObj = {
      children:{},
      type: "folder",
      url: `/proxy/export?container=${commonPath}`
    };
    this.props.data.forEach(item => {
      const path = item.url.split("/").slice(pathArray.length-1);
      this.buildObjectStructure(pathObj, path, item);
    });
    if (pathObj) {
      result.children = [];
      result.toggled = true;
      result.type = "folder";
      result.url = pathObj.url;
      this.constructResult(result, pathObj);
    }
    this.setState({data: {...result} });
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
    const {node, data, showTermsOfUse} = this.state;
    const name = node && node.name;
    const size = node && node.size;
    const type = node && node.type;
    return (
      <div className="kgs-hierarchical-files">
        <Treebeard
          data={data}
          onToggle={this.onToggle}
          style={{...theme}}
        />
        {node.active &&
        <div className="kgs-hierarchical-files__details">
          <div>{node.thumbnail ? <img height="80" src={node.thumbnail} alt={node.url} />:<i className={`fa fa-5x fa-${type}-o`}></i>}</div>
          <div className="kgs-hierarchical-files__info">
            <div>
              <div><strong>Name:</strong> {name}</div>
              {size  && <div><strong>Size:</strong> {size}</div>}
              <a type="button" className="btn kgs-hierarchical-files__info_link" href={node.url} onClick={this.handleClick(node.url)}><i className="fa fa-download"></i> Download {type}</a>
              <div className="kgs-hierarchical-files__info_agreement"><span>By downloading the {type} you agree to the <a href="" onClick={this.toggleTermsOfUse}>Terms of use</a></span></div>
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
