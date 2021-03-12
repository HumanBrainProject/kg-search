/*
*   Copyright (c) 2021, EPFL/Human Brain Project PCO
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
import { Link } from "react-router-dom";
import "./Header.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
export class Header extends React.Component {
  render() {
    const { location, SignInComponent, onClearInstances } = this.props;

    const backToSearch = location => {
      onClearInstances();
      return { ...location, pathname: "/" };
    };

    return (
      <nav className="navbar navbar-expand-lg navbar-light kgs-navbar">
        <div className="container-fluid">
          <a href="https://ebrains.eu" aria-label="EBRAINS homepage" className="logo nuxt-link-active navbar-brand">
            <img src="/static/img/ebrains_logo.svg" alt="ebrains" height="100" />
          </a>
          <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <FontAwesomeIcon icon="bars" />
          </button>

          <div className="collapse navbar-collapse" id="navbarSupportedContent">
            <ul className="navbar-nav mr-auto">
              {location.pathname.startsWith("/instances") && <li className="nav-item"><Link to={location => backToSearch(location)}>Search</Link></li>}
              <li className="nav-item">
                <a href="https://ebrains.eu/services/data-knowledge/share-data"  className="mobile-link" rel="noopener noreferrer">Share data</a>
              </li>
              <li className="nav-item">
                <a href="https://ebrains.eu/service/find-data" className="mobile-link" target="_blank" rel="noopener noreferrer">About</a>
              </li>
              <SignInComponent Tag="li" className="nav-item" />
            </ul>
          </div>
        </div>
      </nav>
    );
  }
}

export default Header;