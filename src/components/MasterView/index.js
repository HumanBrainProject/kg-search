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

import React, { Component } from 'react';
import { SearchkitProvider, Layout, LayoutBody, LayoutResults, NoHits } from 'searchkit';
import { isMobile, tabAblesSelectors } from '../../Helpers/BrowserHelpers';
import { SearchPanel } from './components/SearchPanel';
import { SidePanel } from './components/SidePanel';
import { ResultsHeader } from './components/ResultsHeader';
import { Results } from './components/Results';
import { ResultsFooter } from './components/ResultsFooter';
import { TermsShortNotice } from './components/TermsShortNotice';
import './styles.css';
 
export class MasterView extends Component {
    constructor(props) {
      super(props);
      this.state = {
        showTermsShortNotice: typeof Storage === "undefined" || localStorage.getItem('hbp.kgs-terms-conditions-consent') !== "true",
        gridLayoutMode: true,
        sidepanel: document.documentElement.clientWidth >= 992? "expanded": "collapsed"
      };
      this.componentContext = {
        tabAbles: []
      };
      this.agreeTermsShortNotice = this.agreeTermsShortNotice.bind(this);
      this.setLayoutMode = this.setLayoutMode.bind(this);
      this.toggleSidePanel = this.toggleSidePanel.bind(this);
    }
    agreeTermsShortNotice() {
      if (typeof(Storage) !== "undefined")
        localStorage.setItem('hbp.kgs-terms-conditions-consent', true);
      this.setState({showTermsShortNotice: false});
      setTimeout(() => window.dispatchEvent(new Event('resize')), 250);
    }
    setLayoutMode(gridLayoutMode) {
      this.setState({ gridLayoutMode: !!gridLayoutMode});
    }
    toggleSidePanel() {
      this.setState({ sidepanel: this.state.sidepanel==="collapsed"?"expanded":"collapsed"});
    }
    componentWillUpdate(nextProps, nextState) {
      this.componentContext.tabAbles.forEach(e => {
        if (e.tabIndex >= 0)
          e.node.setAttribute("tabIndex", e.tabIndex);
        else
          e.node.removeAttribute("tabIndex");
      });
    }
    componentDidUpdate(prevProps, prevState) {
      if (!isMobile) {
        //console.log(new Date().toLocaleTimeString() + ": master tabs active=" + this.props.isActive);
        if (!this.props.isActive) {
          const rootNode = document.body.querySelector('.kgs-masterView');
          this.componentContext.tabAbles = Object.values(rootNode.querySelectorAll(tabAblesSelectors.join(',')))
            .map(node => ({node: node, tabIndex: node.tabIndex}));
          this.componentContext.tabAbles
            .forEach(e => e.node.setAttribute("tabIndex", -1));
        }
      }
    }
    render() {
      const { hitCount, hitsPerPage, searchThrottleTime, queryFields, searchkit, currentIndex, indexes, onIndexChange, onSearchError} = this.props;

      const NoHitsDisplay = (props) => {
        return null;
      };
      
      const NoHitsErrorDisplay = (props) => {
        if (searchkit.error) {
          const status = searchkit.error.response && searchkit.error.response.status;
          const nonce = searchkit.transport && searchkit.transport.axios && searchkit.transport.axios.defaults && searchkit.transport.axios.defaults.headers && searchkit.transport.axios.defaults.headers.post && searchkit.transport.axios.defaults.headers.post.nonce;
          onSearchError && onSearchError(status, nonce); 
          delete searchkit.error;
        }
        return null;
      };
      
      const searchPanelRelatedElements = [
        {querySelector: 'body>header'},
        {querySelector: 'body>header + nav.navbar'},
        {querySelector: '#CookielawBanner', cookieKey: 'cookielaw_accepted'}
      ];

      const resultFooterRelatedElements = [
        {querySelector: '.sk-layout__body + .terms-short-notice', localStorageKey: 'hbp.kgs-terms-conditions-consent'},
        {querySelector: '.main-content + hr + .container'},
        {querySelector: 'footer.footer[role="contentinfo'}
      ];

      return (
        <div className="kgs-masterView"  data-layoutMode={this.state.gridLayoutMode?"grid":"list"} data-hasHits={hitCount > 0} data-sidepanel={this.state.sidepanel} >
          <SearchkitProvider searchkit={searchkit}>
            <Layout>
              <SearchPanel searchThrottleTime={searchThrottleTime} queryFields={queryFields} onSidePanelToggle={this.toggleSidePanel} relatedElements={searchPanelRelatedElements} />
              <TermsShortNotice show={this.state.showTermsShortNotice} onAgree={this.agreeTermsShortNotice} />
              <LayoutBody>
                <SidePanel currentIndex={currentIndex} indexes={indexes} onIndexChange={onIndexChange} searchkit={searchkit} onToggle={this.toggleSidePanel} /> 
                <LayoutResults>
                  <ResultsHeader hitCount={hitCount} gridLayoutMode={this.state.gridLayoutMode} onGridLayoutModeToogle={this.setLayoutMode} />
                  <Results hitsPerPage={hitsPerPage} />
                  <ResultsFooter hasPaging={hitCount > hitsPerPage} relatedElements={resultFooterRelatedElements} showTermsShortNotice={this.state.showTermsShortNotice} onAgreeTermsShortNotice={this.agreeTermsShortNotice} />
                </LayoutResults>
              </LayoutBody>
              <NoHits component={NoHitsDisplay} errorComponent={NoHitsErrorDisplay}/>
              <TermsShortNotice show={this.state.showTermsShortNotice} onAgree={this.agreeTermsShortNotice} />
            </Layout>
          </SearchkitProvider>
        </div>
      );
    }
  }