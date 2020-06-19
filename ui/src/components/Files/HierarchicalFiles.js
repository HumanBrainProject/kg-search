import React, {PureComponent} from "react";
import {Treebeard} from "react-treebeard";

// const data = {
//   "name": "react-treebeard",
//   "toggled": false,
//   "children": [
//     {
//       "name": "example",
//       "children": [
//         {
//           "name": "app.js"
//         },
//         {
//           "name": "data.js"
//         },
//         {
//           "name": "index.html"
//         },
//         {
//           "name": "styles.js"
//         },
//         {
//           "name": "webpack.config.js"
//         }
//       ]
//     },
//     {
//       "name": "node_modules",
//       "loading": true,
//       "children": []
//     },
//     {
//       "name": "src",
//       "children": [
//         {
//           "name": "components",
//           "children": [
//             {
//               "name": "decorators.js"
//             },
//             {
//               "name": "treebeard.js"
//             }
//           ]
//         },
//         {
//           "name": "index.js"
//         }
//       ]
//     },
//     {
//       "name": "themes",
//       "children": [
//         {
//           "name": "animations.js"
//         },
//         {
//           "name": "default.js"
//         }
//       ]
//     },
//     {
//       "name": "Gulpfile.js"
//     },
//     {
//       "name": "index.js"
//     },
//     {
//       "name": "package.json"
//     }
//   ],
//   "active": true
// };

export default class HierarchicalFiles extends PureComponent {
  constructor(props){
    super(props);
    this.state = {
      curson: false,
      data: {}
    };
  }

  componentDidMount() {
    this.constructData();
  }

  buildObjectStructure = (obj,path) => path.forEach(d => obj = obj[d] || (obj[d]={}));

  constructResult = (resultObj, val) => {
    if(val) {
      Object.entries(val).forEach(([key, value]) => {
        if(key.includes(".")) {
          resultObj.children.push({
            name: key
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
    const lastElement= sortedArray[sortedArray.length-1];
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
      this.buildObjectStructure(pathObj, path);
    });
    if (pathObj) {
      result.children = [];
      this.constructResult(result, pathObj);
    }
    this.setState({data: Object.assign({}, result)});
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
    this.setState({cursor: node, data: Object.assign({}, data)});
  }

  render(){
    const {data} = this.state;
    return (
      <Treebeard
        data={data}
        onToggle={this.onToggle}
      />
    );
  }
}
