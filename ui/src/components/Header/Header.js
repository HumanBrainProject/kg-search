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
import { SignIn } from "../../containers/SignIn/SignIn";

export class Header extends React.Component {
  render() {
    const { location } = this.props;
    return (
      <nav className="navbar navbar-default kgs-navbar">
        <div className="container-fluid">
          <div className="navbar-header">
            <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target="#nav-collapse-mobile" aria-expanded="false">
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
            </button>
            <a href="https://ebrains.eu" aria-label="EBRAINS homepage" className="logo nuxt-link-active navbar-brand">
              <img src="/static/img/ebrains_logo.svg" alt="ebrains" height="100" />
            </a>
          </div>

          <div className="collapse navbar-collapse" id="nav-collapse-mobile">
            <ul className="nav navbar-nav navbar-right">
              {location.pathname.startsWith("/instances") && <li><Link to="/">Search</Link></li>}
              <li><a href="https://ebrains.eu/services/data-knowledge/share-data"  className="mobile-link" rel="noopener noreferrer">Share data</a></li>
              <li><a href="https://ebrains.eu/service/find-data" className="mobile-link" target="_blank" rel="noopener noreferrer">About</a></li>
              <li><SignIn /></li>
            </ul>
          </div>
        </div>
      </nav>
    );
  }
}

export default Header;