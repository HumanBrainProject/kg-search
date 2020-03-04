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
import uniqueId from "lodash/uniqueId";
import renderer from "react-test-renderer";
import Enzyme, { mount, shallow, render } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import Select from "./Select";

Enzyme.configure({ adapter: new Adapter() });

test('Select component renders initially', () => {
    const component = renderer.create(
        <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});
  
test('Select test className', () => {
    const component = shallow(
        <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
    );
    expect(component.hasClass("className"));
});

test('Select test label', () => {
    const component = shallow(
        <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
    );
    expect(component.find("div div").text()).toBe("a label");
});

test('Select test number of items', () => {
    const component = render(
        <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
    );
    expect(component.find("option").length).toBe(2);
});

test('Select test onchange', () => {
    const fn = jest.fn();
    const component = mount(
        <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={fn} />
    );
    component.find('select').simulate('change', {target: { value : 'another value'}});
    expect(fn.mock.calls.length).toBe(1);
    expect(fn.mock.calls[0][0]).toBe("another value");
});