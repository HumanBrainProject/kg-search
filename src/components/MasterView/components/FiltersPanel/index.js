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
import { ResetFilters } from "searchkit";
import { RefinementListFilter, InputFilter, CheckboxFilter, RangeFilter } from "searchkit";
import { DateRangeFilter, DateRangeCalendar } from "searchkit-datefilter";
import { store } from "../../../../store";
import "./styles.css";

const FacetList = ({searchkit}) => {
  const state = store.getState();
  const facetFields = state.configuration.facetFields;
  let facetsRender = [];
  const selectedType = searchkit.state.facet_type && searchkit.state.facet_type.length > 0 ? searchkit.state.facet_type[0]: "";

  Object.keys(facetFields).forEach(type => {
    const currentTypeSelected = selectedType === type;
    Object.keys(facetFields[type]).forEach((facetField)=>{
      const facet = facetFields[type][facetField];
      const fieldKey = "facet_"+type+"_"+facetField;
      if(facet.filterType === "list"){
        const orderKey = facet.filterOrder && facet.filterOrder === "byvalue"? "_term": "_count";
        const operator = facet.exclusiveSelection === false?"OR":"AND";
        const orderDirection = orderKey === "_term"? "asc": "desc";
        if(facet.isChild){
          facetsRender.push(<div className={!currentTypeSelected?"hidden":null} key={fieldKey}>
            <RefinementListFilter
              id={fieldKey}
              field={facetField+".value.keyword"}
              title={facet.fieldLabel}
              operator={operator}
              size={10}
              orderKey={orderKey}
              orderDirection={orderDirection}
              fieldOptions={{type:"nested", options:{path:facet.path}}}/>
          </div>);
        } else {
          facetsRender.push(<div className={!currentTypeSelected?"hidden":null} key={fieldKey}>
            <RefinementListFilter
              id={fieldKey}
              field={facetField+".value.keyword"}
              title={facet.fieldLabel}
              operator={operator}
              size={10}
              orderKey={orderKey}
              orderDirection={orderDirection}/>
          </div>);
        }
      } else if(facet.filterType === "input"){
        facetsRender.push(<div className={!currentTypeSelected?"hidden":null} key={fieldKey}>
          <InputFilter
            key={fieldKey}
            id={fieldKey}
            title={facet.fieldLabel}
            placeholder={"Search "+facet.fieldLabel}
            searchOnChange={true}
            queryFields={[facetField+".value.keyword"]}/>
        </div>);
      } else if(facet.filterType === "exists"){
        facetsRender.push(<div className={!currentTypeSelected?"hidden":null} key={fieldKey}>
          <CheckboxFilter
            key={fieldKey}
            id={fieldKey}
            title={facet.fieldLabel}
            label={"Has "+facet.fieldLabel.toLowerCase()}
            filter={{exists:{field:facetField+".value.keyword"}}} />
        </div>);
      } else if(facet.filterType === "range"){
        if(facet.fieldType === "date"){
          facetsRender.push(<div className={!currentTypeSelected?"hidden":null} key={fieldKey}>
            <DateRangeFilter
              key={fieldKey}
              id={fieldKey}
              fromDateField={facetField+".value"}
              toDateField={facetField+".value"}
              calendarComponent={DateRangeCalendar}
              title={facet.fieldLabel}/>
          </div>);
        } else {
          facetsRender.push(<div className={!currentTypeSelected?"hidden":null} key={fieldKey}>
            <RangeFilter
              key={fieldKey}
              id={fieldKey}
              field={facetField+".value"}
              min={0}
              max={200}
              showHistogram={true}
              title={facet.fieldLabel}/>
          </div>);
        }
      }
    });
  });
  return <span>{facetsRender}</span>;
};

export function FiltersPanel({searchkit}) {
  return (
    <div className="kgs-filters">
      <div className="kgs-filters__header">
        <div className="kgs-filters__title">Filters</div>
        <div className="kgs-filters__reset"><ResetFilters options={{query:false, filter:true, pagination:true}} translations={{"reset.clear_all":"Reset"}}/></div>
      </div>
      <FacetList searchkit={searchkit} />
    </div>
  );
}
