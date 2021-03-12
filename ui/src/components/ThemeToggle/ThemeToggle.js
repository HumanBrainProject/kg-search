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

import React, { useState, useEffect } from "react";
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

export const ThemeToggle = () => {
  const [theme, setTheme] = useState();

  const applyTheme = value => {
    if (value) {
      document.body.setAttribute("theme", value);
      localStorage.setItem("currentTheme", value);
    } else {
      document.body.removeAttribute("theme");
      localStorage.removeItem("currentTheme", value);
    }
  };

  useEffect(() => {
    const value = localStorage.getItem("currentTheme");
    applyTheme(value);
    setTheme(value);
  }, []);

  const handleClick = v => {
    const value = v.default?null:v.name;
    applyTheme(value);
    setTheme(value);
  };

  return (
    <div className="kgs-theme_toggle">
      {themes.map(t => (
        <button key={t.name} className={`kgs-theme_toggle__button ${((!theme && t.default) || (t.name === theme))?"selected":""}`} onClick={() => handleClick(t)}>
          <FontAwesomeIcon icon={t.icon} />
        </button>
      ))}
    </div>
  );
};

export default ThemeToggle;