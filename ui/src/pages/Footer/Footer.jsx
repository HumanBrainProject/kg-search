/*
 * Copyright 2018 - 2021 Swiss Federal Institute of Technology Lausanne (EPFL)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This open source software code was developed in part or in whole in the
 * Human Brain Project, funded from the European Union's Horizon 2020
 * Framework Programme for Research and Innovation under
 * Specific Grant Agreements No. 720270, No. 785907, and No. 945539
 * (Human Brain Project SGA1, SGA2 and SGA3).
 *
 */
import React from 'react';
import { connect } from 'react-redux';
import ThemeToggle from '../../features/theme/ThemeToggle';

import './Footer.css';
import profiles from "../../data/profiles";

const Footer = ({ commit, profile, theme }) => (
  <footer className="site-footer">
    <div className="footer__header">
      <div className="footer__primary">


          {profile !== 'ebrains' &&
              <span className="service-at"><a href={profiles[profile]["home"]} aria-label="Homepage" title="Homepage" className="logo nuxt-link-exact-active nuxt-link-active"> <img src={`/static/img/${theme === 'dark' ? profiles[profile]["logo_dark"] : profiles[profile]["logo"]}`}
                                                                                                                                                              height="100" /></a> @ </span>}
                <a href="https://ebrains.eu" aria-label="Ebrains homepage" title="Ebrains homepage" className="logo nuxt-link-exact-active nuxt-link-active"><img
                    src={`/static/img/${theme === 'dark' ? "ebrains_logo_dark.svg" : 'ebrains_logo.svg'}`}
                    height="100"/>
                </a>
              </div>
            </div>
            <div className="footer__content">
            <div className="footer__secondary">
            <p>EBRAINS is powered by the EU-cofunded Human Brain Project</p>
        <div className="powered-by">
          <a className="hbp-logo" href="https://www.humanbrainproject.eu/" title="Human Brain Project" target="_blank" rel="noopener noreferrer">
            <img src="/static/img/hbp.svg" alt="Human Brain Project" />
          </a>
          <a className="eu-logo" href="https://ec.europa.eu/programmes/horizon2020/en/h2020-section/fet-flagships" title="Cofunded by EU"  target="_blank" rel="noopener noreferrer">
            <img src="/static/img/eu_cofunded_logo.png" alt="Cofunded by EU" />
          </a>
        </div>
              {profile==="ebrains" && <ThemeToggle />}
      </div>
      <div className="footer__menu footer__menu-1">
        <ul>
          <li className="title">
            <a href="https://ebrains.eu/services">Services</a>
          </li>
          <li>
            <a href="https://ebrains.eu/services/data-and-knowledge">Data and Knowledge</a>
          </li>
          <li>
            <a href="https://ebrains.eu/services/atlases">Atlases</a>
          </li>
          <li>
            <a href="https://ebrains.eu/services/simulation">Simulation</a>
          </li>
          <li>
            <a href="https://ebrains.eu/services/brain-inspired-technologies">Brain-Inspired Technologies</a>
          </li>
          <li>
            <a href="https://ebrains.eu/services/medical-data">Medical Data Analytics</a>
          </li>
        </ul>
      </div>
      <div className="footer__menu footer__menu-2">
        <ul>
          <li className="title">Contact us</li>
          <li>
            <a href="https://ebrains.eu/support">Support</a>
          </li>
          <li>
            <a href="https://ebrains.eu/register">Register</a>
          </li>
        </ul>
      </div>
      <div className="footer__menu footer__menu-3">
        <ul>
          <li className="title">
            <a href="https://ebrains.eu/about">About EBRAINS</a>
          </li>
          <li>
            <a href="https://ebrains.eu/terms">Terms and Policies</a>
          </li>
        </ul>
      </div>
    </div>
    <hr className="full-width" />
    <div className="footer__end">
      <div className="footer__copyright">
        &copy;{profiles[profile]["copyrightSince"] !== new Date().getFullYear().toString() && profiles[profile]["copyrightSince"]+'-'}{new Date().getFullYear()}&nbsp;{profiles[profile]["copyright"]}
      </div>
      <div className="commit">
        {commit && <span>build: <i>{commit}</i></span>}
      </div>
      <ul className="footer__social">
        <li>
          <a href="https://www.facebook.com/Ebrains_eu-112559066821827" target="_blank" rel="noopener noreferrer" title="Facebook">
            <svg className="sprite-icons" xmlns="http://www.w3.org/2000/svg" id="i-facebook" viewBox="0 0 40 40"
              fill="none">
              <path d="M26 10h-3a5 5 0 00-5 5v3h-3v4h3v8h4v-8h3l1-4h-4v-3a1 1 0 011-1h3v-4z" stroke="currentColor"
                strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </a>
        </li>
        <li>
          <a href="https://twitter.com/ebrains_eu" target="_blank" rel="noopener noreferrer" title="Twitter">
            <svg className="sprite-icons" xmlns="http://www.w3.org/2000/svg" id="i-twitter" viewBox="0 0 40 40" fill="none">
              <path
                d="M31 11a10.897 10.897 0 01-3.14 1.53 4.48 4.48 0 00-7.86 3v1A10.66 10.66 0 0111 12s-4 9 5 13a11.64 11.64 0 01-7 2c9 5 20 0 20-11.5 0-.278-.028-.556-.08-.83A7.72 7.72 0 0031 11v0z"
                stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </a>
        </li>
        {/* <li>
            <a href="https://www.youtube.com/user/TheHumanBrainProject" target="_blank" rel="noopener" title="Youtube">
              <svg className="sprite-icons" xmlns="http://www.w3.org/2000/svg" id="i-youtube" viewBox="0 0 40 40" fill="none">
                <path d="M30.54 14.42a2.78 2.78 0 00-1.94-2C26.88 12 20 12 20 12s-6.88 0-8.6.46a2.78 2.78 0 00-1.94 2A29.001 29.001 0 009 19.75a29 29 0 00.46 5.33A2.78 2.78 0 0011.4 27c1.72.46 8.6.46 8.6.46s6.88 0 8.6-.46a2.78 2.78 0 001.94-2c.312-1.732.466-3.49.46-5.25a29.005 29.005 0 00-.46-5.33v0z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"></path>
                <path d="M17.75 23.02l5.75-3.27-5.75-3.27v6.54z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"></path>
              </svg>
            </a>
          </li> */}
        <li>
          <a href="https://www.instagram.com/ebrains_eu/" target="_blank" rel="noopener noreferrer" title="Instagram">
            <svg className="sprite-icons" xmlns="http://www.w3.org/2000/svg" id="i-instagram" viewBox="0 0 40 40" fill="none">
              <path d="M25 10H15a5 5 0 00-5 5v10a5 5 0 005 5h10a5 5 0 005-5V15a5 5 0 00-5-5z" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M24 19.37a4 4 0 11-7.914 1.173A4 4 0 0124 19.37zM25.5 14.5h.01" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </a>
        </li>
        {/* <li>
          <a href="https://github.com/HumanBrainProject/" target="_blank" rel="noopener" title="GitHub">
            <svg className="sprite-icons" xmlns="http://www.w3.org/2000/svg" id="i-github" viewBox="0 0 40 40" fill="none">
              <path d="M24 30v-3.87a3.37 3.37 0 00-.94-2.61c3.14-.35 6.44-1.54 6.44-7a5.44 5.44 0 00-1.5-3.75A5.07 5.07 0 0027.91 9s-1.18-.35-3.91 1.48a13.38 13.38 0 00-7 0C14.27 8.65 13.09 9 13.09 9a5.07 5.07 0 00-.09 3.77 5.44 5.44 0 00-1.5 3.78c0 5.42 3.3 6.61 6.44 7a3.37 3.37 0 00-.94 2.58V30m0-3c-5 1.5-5-2.5-7-3l7 3z" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"></path>
            </svg>
          </a>
        </li> */}
        <li>
          <a href="https://www.linkedin.com/company/ebrains-eu" target="_blank" rel="noopener noreferrer" title="Linkedin">
            <svg className="sprite-icons" xmlns="http://www.w3.org/2000/svg" id="i-linkedin" viewBox="0 0 40 40" fill="none">
              <path d="M24 16a6 6 0 016 6v7h-4v-7a2 2 0 00-4 0v7h-4v-7a6 6 0 016-6v0zM14 17h-4v12h4V17zM12 14a2 2 0 100-4 2 2 0 000 4z" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </a>
        </li>
      </ul>
    </div>
  </footer>
);

export default connect(
  state => ({
    commit: state.application.commit,
    profile: state.application.profile,
    theme: state.application.theme
  })
)(Footer);