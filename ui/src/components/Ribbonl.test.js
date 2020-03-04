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
import Ribbon from "./Ribbon";

Enzyme.configure({ adapter: new Adapter() });

test('Ribbon component renders initially', () => {
    const component = renderer.create(
        <Ribbon className="className" icon="an icon" text="some text" counter={12} suffix="a suffix" />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('Ribbon test className"', () => {
    const component = shallow(
        <Ribbon className="className" icon="an icon" text="some text" counter={12} suffix="a suffix" />
    );
    expect(component.hasClass("className"));
});

test('Ribbon test icon', () => {
    const component = shallow(
        <Ribbon className="className" icon="an icon" text="some text" counter={12} suffix="a suffix" />
    );
    expect(component.find("div.ribbon-inner-content div").at(0).html()).toContain("an icon");
});

test('Ribbon test text', () => {
    const component = shallow(
        <Ribbon className="className" icon="an icon" text="some text" counter={12} suffix="a suffix" />
    );
    expect(component.find("div.ribbon-inner-content div").at(1).html()).toContain("some text");
});

test('Ribbon test counter & suffix', () => {
    const component = shallow(
        <Ribbon className="className" icon="an icon" text="some text" counter={12} suffix="a suffix" />
    );
    expect(component.find(".ribbon-inner-content-framed").html()).toContain("12 a suffix");
});
