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
import List from "./List";

Enzyme.configure({ adapter: new Adapter() });

test('List component renders initially', () => {
    const component = renderer.create(
        <List className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => null} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});
  
test('List test className"', () => {
    const component = shallow(
        <List className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => null} />
    );
    expect(component.hasClass("className"));
});

test('List test title"', () => {
    const component = shallow(
        <List className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => null} />
    );
    expect(component.find("div div").text()).toBe("a title");
});

test('List test number of items', () => {
    const component = render(
        <List className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => null} />
    );
    expect(component.find("li").length).toBe(2);
});

test('Toggle test  button click', () => {
    const fn = jest.fn();
    const component = mount(
        <List className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={fn} itemComponent={() => null} />
    );
    component.find('button').at(1).simulate('click');
    expect(fn.mock.calls.length).toBe(1);
    expect(fn.mock.calls[0][0].value).toBe("another value");
});