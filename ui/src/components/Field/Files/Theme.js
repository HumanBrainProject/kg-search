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


export default {
  tree: {
    base: {
      listStyle: "none",
      backgroundColor: "transparent",
      margin: 0,
      padding: 0,
      color: "#9DA5AB",
      fontFamily: "lucida grande ,tahoma,verdana,arial,sans-serif",
      fontSize: "14px"
    },
    node: {
      base: {
        position: "relative"
      },
      link: {
        cursor: "pointer",
        position: "relative",
        padding: "0px 5px",
        display: "block"
      },
      activeLink: {
        background: "var(--button-default-background-color)"
      },
      toggle: {
        base: {
          position: "relative",
          display: "inline-block",
          verticalAlign: "top",
          marginLeft: "-5px",
          height: "24px",
          width: "24px"
        },
        wrapper: {
          position: "absolute",
          top: "50%",
          left: "50%",
          margin: "-12px 0 0 -7px",
          height: "14px"
        },
        height: 14,
        width: 14,
        arrow: {
          fill: "#9DA5AB",
          strokeWidth: 0
        }
      },
      header: {
        base: {
          display: "inline-block",
          verticalAlign: "top",
          color: "#9DA5AB"
        },
        connector: {
          width: "2px",
          height: "12px",
          borderLeft: "solid 2px black",
          borderBottom: "solid 2px black",
          position: "absolute",
          top: "0px",
          left: "-21px"
        },
        title: {
          lineHeight: "24px",
          verticalAlign: "middle"
        }
      },
      subtree: {
        listStyle: "none",
        paddingLeft: "19px"
      },
      loading: {
        color: "#E2C089"
      }
    }
  }
};