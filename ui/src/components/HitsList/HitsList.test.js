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
import uniqueId from "lodash/uniqueId";
import renderer from "react-test-renderer";
import Enzyme, { mount, shallow, render } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import HitsList from "./HitsList";

Enzyme.configure({ adapter: new Adapter() });

test('HitsList component renders initially', () => {
    const component = renderer.create(
        <HitsList className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => (<div></div>)} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});
  
test('HitsList test className"', () => {
    const component = shallow(
        <HitsList className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => (<div></div>)} />
    );
    expect(component.hasClass("className"));
});

test('HitsList test title"', () => {
    const component = shallow(
        <HitsList className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => (<div></div>)} />
    );
    expect(component.find("div div").text()).toBe("a title");
});

test('HitsList test number of items', () => {
    const component = render(
        <HitsList className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={() => {}} itemComponent={() => (<div></div>)} />
    );
    expect(component.find("li").length).toBe(2);
});

test('Toggle test  button click', () => {
    const fn = jest.fn();
    const component = mount(
        <HitsList className="className" title="a title" items={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} getKey={() => uniqueId()} onClick={fn} itemComponent={() => (<div></div>)} />
    );
    component.find('button').at(1).simulate('click');
    expect(fn.mock.calls.length).toBe(1);
    expect(fn.mock.calls[0][0].value).toBe("another value");
});