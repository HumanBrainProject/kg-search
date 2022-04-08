import React from "react";
import { connect } from "react-redux";
import { Link } from "react-router-dom";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

import { SignIn } from "../SignIn/SignIn";
import * as actionsInstances from "../../actions/actions.instances";

import "./Header.css";

export const Header = ({ location, SignInComponent, onClearInstances, theme }) => {
  const backToSearch = location => {
    onClearInstances();
    return { ...location, pathname: "/" };
  };

  return (
    <nav className="navbar navbar-expand-lg navbar-light kgs-navbar">
      <div className="container-fluid">
        <a href="https://ebrains.eu" aria-label="EBRAINS homepage" className="logo nuxt-link-active navbar-brand">
          <img src={`/static/img/${theme === "dark"?"ebrains_logo_dark.svg":"ebrains_logo.svg"}`} alt="ebrains" height="100" />
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
};

export default connect(
  state => ({
    location: state.router.location,
    SignInComponent: SignIn,
    theme: state.application.theme
  }),
  dispatch => ({
    onClearInstances: () => dispatch(actionsInstances.clearAllInstances())
  })
)(Header);