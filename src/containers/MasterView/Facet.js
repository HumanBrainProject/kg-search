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
import { RefinementListFilter, InputFilter, CheckboxFilter, RangeFilter } from "searchkit";
/* No date range filter: package removed for now */
/* import { DateRangeFilter, DateRangeCalendar } from "searchkit-datefilter"; */
import "./Facet.css";

const SearchkitFacet = ({id, name, facet}) => {
  if (facet.filterType === "list") {
    const orderKey = facet.filterOrder && facet.filterOrder === "byvalue"? "_term": "_count";
    const operator = facet.exclusiveSelection === false?"OR":"AND";
    const orderDirection = orderKey === "_term"? "asc": "desc";
    if (facet.isChild) {
      return (
        <RefinementListFilter
          id={id}
          field={name+".value.keyword"}
          title={facet.fieldLabel}
          operator={operator}
          size={10}
          orderKey={orderKey}
          orderDirection={orderDirection}
          fieldOptions={{type:"nested", options:{path:facet.path}}} />
      );
    }
    return (
      <RefinementListFilter
        id={id}
        field={name+".value.keyword"}
        title={facet.fieldLabel}
        operator={operator}
        size={10}
        orderKey={orderKey}
        orderDirection={orderDirection}/>
    );
  }
  if (facet.filterType === "input") {
    return (
      <InputFilter
        id={id}
        title={facet.fieldLabel}
        placeholder={"Search "+facet.fieldLabel}
        searchOnChange={true}
        queryFields={[name+".value.keyword"]} />
    );
  }
  if (facet.filterType === "exists"){
    return (
      <CheckboxFilter
        id={id}
        title={facet.fieldLabel}
        label={"Has "+facet.fieldLabel.toLowerCase()}
        filter={{exists:{field:name+".value.keyword"}}} />
    );
  }
  if(facet.filterType === "range"){
    /*
    if(facet.fieldType === "date") {
      return (
        <DateRangeFilter
          id={id}
          fromDateField={name+".value"}
          toDateField={name+".value"}
          calendarComponent={DateRangeCalendar}
          title={facet.fieldLabel} />
      );
    }
    */
    return (
      <RangeFilter
        id={id}
        field={name+".value"}
        min={0}
        max={200}
        showHistogram={true}
        title={facet.fieldLabel} />
    );
  }
  return null;
};

export const Facet = ({className, id, name, facet, isVisible}) => {
  const classNames = ["kgs-facet", className, isVisible?"":"hidden"].join(" ");
  return (
    <div className={classNames}>
      <SearchkitFacet id={id} name={name} facet={facet} />
    </div>
  );
};