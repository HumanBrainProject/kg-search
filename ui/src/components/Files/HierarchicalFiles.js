import React, {PureComponent} from "react";
import {Treebeard} from "react-treebeard";

const data = {
  "name": "react-treebeard",
  "toggled": false,
  "children": [
    {
      "name": "example",
      "children": [
        {
          "name": "app.js"
        },
        {
          "name": "data.js"
        },
        {
          "name": "index.html"
        },
        {
          "name": "styles.js"
        },
        {
          "name": "webpack.config.js"
        }
      ]
    },
    {
      "name": "node_modules",
      "loading": true,
      "children": []
    },
    {
      "name": "src",
      "children": [
        {
          "name": "components",
          "children": [
            {
              "name": "decorators.js"
            },
            {
              "name": "treebeard.js"
            }
          ]
        },
        {
          "name": "index.js"
        }
      ]
    },
    {
      "name": "themes",
      "children": [
        {
          "name": "animations.js"
        },
        {
          "name": "default.js"
        }
      ]
    },
    {
      "name": "Gulpfile.js"
    },
    {
      "name": "index.js"
    },
    {
      "name": "package.json"
    }
  ],
  "active": true
};

export default class HierarchicalFiles extends PureComponent {
  constructor(props){
    super(props);
    this.constructData();
    this.state = {
      data
    };
  }

  build = (obj,path) => path.map(d => {
    return obj = obj[d] || (obj[d]={});
  });

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
        resultObj.children.forEach(c => {
          if(c.name === key){
            this.constructResult(c, value);
          }
        });
      });
    }
  }

  constructData = () => {
    const root = this.props.data[0].url.split("/").slice(6)[0];
    const result = {
      name: root,
      children: []
    };
    const obj = {};
    this.props.data.forEach(item => {
      const path = item.url.split("/").slice(7);
      this.build(obj, path);
    });
    console.log(obj);
    Object.entries(obj).forEach(([key, value]) => {
      if(!key.includes(".")) {
        result.children.push({
          name: key,
          children: []
        });
      }
      result.children.forEach(child => {
        if(child.name === key){
          this.constructResult(child, value);
        }
      });
    });
    console.log(result);
  }

  onToggle = (node, toggled) => {
    const {cursor, data} = this.state;
    if (cursor) {
      this.setState(() => ({cursor, active: false}));
    }
    node.active = true;
    if (node.children) {
      node.toggled = toggled;
    }
    this.setState(() => ({cursor: node, data: Object.assign({}, data)}));
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
