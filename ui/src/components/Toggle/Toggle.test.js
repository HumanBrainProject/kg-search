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

import React from "react";
import renderer from "react-test-renderer";
import Enzyme, { mount, shallow, render } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import Toggle from "./Toggle";

Enzyme.configure({ adapter: new Adapter() });

test('Toggle component renders initially', () => {
    const component = renderer.create(
        <Toggle className="className" show={true} value="a value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('Toggle test show false"', () => {
    const component = renderer.create(
        <Toggle className="className" show={false} value="a value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.toJSON()).toBe(null);
});
  
test('Toggle test className"', () => {
    const component = shallow(
        <Toggle className="className" show={true} value="a value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.hasClass("className"));
});

test('Toggle test number of items', () => {
    const component = render(
        <Toggle className="className" show={true} value="a value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.find("button").length).toBe(2);
});

test('Toggle test item label', () => {
    const component = mount(
        <Toggle className="className" show={true} value="a value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.find("button").at(0).text()).toEqual("a label");
    expect(component.find("button").at(1).text()).toEqual("another label");
});

test('Toggle test active item', () => {
    const component = mount(
        <Toggle className="className" show={true} value="a value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.find("button").at(0).hasClass("is-active"));
    expect(component.find("button").at(1).hasClass("is-active")).toBe(false);
});

test('Toggle test active item', () => {
    const component = mount(
        <Toggle className="className" show={true} value="another value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.find("button").at(0).hasClass("is-active")).toBe(false);
    expect(component.find("button").at(1).hasClass("is-active"));
});

test('Toggle test no active items', () => {
    const component = mount(
        <Toggle className="className" show={true} value="a third value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={() => {}} />
    );
    expect(component.find("button").at(0).hasClass("is-active")).toBe(false);
    expect(component.find("button").at(1).hasClass("is-active")).toBe(false);
});

test('Toggle test  button click', () => {
    const fn = jest.fn();
    const component = mount(
        <Toggle className="className" show={true} value="a third value" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onClick={fn} />
    );
    component.find('button').at(1).simulate('click');
    expect(fn.mock.calls.length).toBe(1);
    expect(fn.mock.calls[0][0]).toBe("another value");
});