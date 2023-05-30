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
import React, { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import FieldsPanel from '../../../components/Field/FieldsPanel';
import { ImagePreviews } from '../../../features/image/ImagePreviews';
import './Tabs.css';
import './Overview.css';
import { setTab } from '../../../features/instance/instanceSlice';
import { Field } from '../../Field/Field';

const Tab = ({tab, active, onClick}) => {

  const handleClick = () => onClick(tab.name);

  const className = `kgs-tabs__button ${active?'is-active':''}`;
  return (
    <button type="button" className={className} onClick={handleClick}>{tab.name?tab.name:''}</button>
  );
};


const TabsView = ({tab}) => {
  if (!tab || !Array.isArray(tab.fields)) {
    return null;
  }

  if (tab.name === 'Overview') {
    const previews = tab.previews;
    const summaryFields = tab.fields.filter(f => f.mapping.layout === 'summary');

    return (
      <div className={`kgs-tabs__view kgs-tabs__overview ${(previews && previews.length) ? 'kgs-tabs__overview__with-previews' : ''}  ${(summaryFields && summaryFields.length) ? 'kgs-tabs__overview__with-summary' : ''}`}>
        <ImagePreviews className={`kgs-tabs__overview__previews ${(previews && previews.length > 1) ? 'has-many' : ''}`} width="300px" images={previews} />
        <FieldsPanel className="kgs-tabs__overview__summary" fields={summaryFields} fieldComponent={Field} />
        <FieldsPanel className="kgs-tabs__overview__main" fields={tab.fields} fieldComponent={Field} />
      </div>
    );
  }

  return (
    <FieldsPanel className="kgs-tabs__view" fields={tab.fields} fieldComponent={Field} />
  );
};

const Tabs = ({tabs, selectedTab, onTabClick }) => {
  const dispatch = useDispatch();
  const hasContent = Array.isArray(tabs) && tabs.length>0;
  let activeTab = selectedTab?tabs.find(t => t.name === selectedTab):null;
  if (!activeTab && hasContent) {
    activeTab = tabs[0];
  }
  useEffect(() => {
    if(activeTab && selectedTab !== activeTab.name) {
      dispatch(setTab(activeTab.name));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  if (!hasContent) {
    return null;
  }

  return (
    <>
      <div className="kgs-tabs__buttons">
        {tabs.map(t => (
          <Tab key={t.name} tab={t} active={t && t.name === activeTab.name} onClick={onTabClick} />
        ))}
      </div>
      <div className="kgs-tabs__content">
        <TabsView tab={activeTab}/>
      </div>
    </>
  );
};

export default Tabs;