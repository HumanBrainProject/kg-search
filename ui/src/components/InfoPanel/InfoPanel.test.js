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
import InfoPanel from "./InfoPanel";

Enzyme.configure({ adapter: new Adapter() });

test('InfoPanel component renders initially', () => {
    const component = renderer.create(
        <InfoPanel text="some text" onClose={() => {}} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('InfoPanel test text', () => {
    const component = shallow(
        <InfoPanel text="some text" onClose={() => {}} />
    );
    expect(component.find("span").html()).toContain("some text");
});

test('InfoPanel test cancel button click', () => {
    const fn = jest.fn();
    const component = shallow(
        <InfoPanel text="some text" onClose={fn} />
    );
    component.find('button').simulate('click');
    expect(fn.mock.calls.length).toBe(1);
});