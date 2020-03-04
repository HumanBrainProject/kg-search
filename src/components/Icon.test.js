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
import renderer from "react-test-renderer";
import Enzyme, { shallow } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import Icon from "./Icon";

Enzyme.configure({ adapter: new Adapter() });

test('Icon component renders initially', () => {
    const component = renderer.create(
        <Icon />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('Icon test className"', () => {
    const component = shallow(
        <Icon className="test" />
    );
    expect(component.hasClass("test"));
});
  
test('Icon test without props', () => {
    const component = shallow(
        <Icon />
    );
    expect(component.find("i").exists()).toEqual(true);
});

test('Icon test url', () => {
    const component = shallow(
        <Icon url="url"/>
    );
    expect(component.find("img").prop("src")).toEqual("url");
});

test('Icon test title', () => {
    const component = shallow(
        <Icon url="url" title="title"/>
    );
    expect(component.find("img").prop("alt")).toEqual("title");
});

test('Icon test inline', () => {
    const component = shallow(
        <Icon inline="inline"/>
    );
    expect(component.find("div").html()).toContain("inline");
});
