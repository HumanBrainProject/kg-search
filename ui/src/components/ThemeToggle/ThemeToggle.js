/*
*   Copyright (c) 2018, EPFL/Human Brain Project PCO
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
import "./ThemeToggle.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

const themes = [
  {
    name: "dark",
    icon: "moon"
  },
  {
    name: "default",
    icon: "sun",
    default: true
  }
];

export class ThemeToggle extends React.Component {
  constructor(props) {
    super(props);
    const theme = localStorage.getItem("currentTheme");
    this.setTheme(theme);
    this.state = {
      theme: theme
    };
  }

  setTheme = theme => {
    if (theme) {
      document.body.setAttribute("theme", theme);
      localStorage.setItem("currentTheme", theme);
    } else {
      document.body.removeAttribute("theme");
      localStorage.removeItem("currentTheme", theme);
    }
  }

  handleClick = theme => {
    const value = theme.default?null:theme.name;
    this.setTheme(value);
    this.setState({theme: value});
  }

  render() {
    return (
      <div className="kgs-theme_toggle">
        {themes.map(theme => (
          <button key={theme.name} className={`kgs-theme_toggle__button ${((!this.state.theme && theme.default) || (theme.name === this.state.theme))?"selected":""}`} onClick={() => this.handleClick(theme)}>
            <FontAwesomeIcon icon={theme.icon} />
          </button>
        ))}
      </div>
    );
  }
}

export default ThemeToggle;