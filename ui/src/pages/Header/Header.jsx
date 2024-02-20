import {faBars} from '@fortawesome/free-solid-svg-icons/faBars';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useLocation, useNavigate } from 'react-router-dom';

import SignIn from '../../features/auth/SignIn';
import { reset } from '../../features/instance/instanceSlice';


import './Header.css';

import profiles from '../../data/profiles';

const Header = () => {

  const location = useLocation();
  const navigate = useNavigate();

  const dispatch = useDispatch();

  const theme = useSelector(state => state.application.theme);
  const profile = useSelector(state => state.application.profile);
  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);


  const handleSearchClick = () => {
    dispatch(reset());
    navigate(`/${group !== defaultGroup?('?group=' + group):''}`);
  };

  const showSearchLink  = location.pathname.startsWith('/instances');

  return (
    <nav className="navbar navbar-expand-lg navbar-light kgs-navbar">
      <div className="container-fluid">
        <a href={profiles[profile]["home"]} aria-label="EBRAINS homepage" className="logo nuxt-link-active navbar-brand">
          <img src={`/static/img/${theme === 'dark'? profile+'_logo_dark.svg': profile+'_logo.svg'}`} alt="ebrains" height="100" />
        </a>
        <button className="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
          <FontAwesomeIcon icon={faBars} />
        </button>

        <div className="collapse navbar-collapse" id="navbarSupportedContent">
          <ul className="navbar-nav mr-auto">
            {showSearchLink && <li className="nav-item"><button role="link" className="mobile-link" onClick={handleSearchClick}>Search</button></li>}
            {profile==="ebrains" &&
            <li className="nav-item">
              <a href="https://www.ebrains.eu/data/share-data"  className="mobile-link" rel="noopener noreferrer">Share data</a>
            </li>}
            {profile==="ebrains" &&
            <li className="nav-item">
              <a href="https://www.ebrains.eu/data/find-data" className="mobile-link" target="_blank" rel="noopener noreferrer">About</a>
            </li> }
            <SignIn Tag="li" className="nav-item" />
          </ul>
        </div>
      </div>
    </nav>
  );
};

export default Header;
