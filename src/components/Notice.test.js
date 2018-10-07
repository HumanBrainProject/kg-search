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
import Notice from "./Notice";

Enzyme.configure({ adapter: new Adapter() });

test('Notice component renders initially', () => {
    const component = renderer.create(
        <Notice className="className" show={true} text="some text" agreeLabel="agree label" onAgree={() => {}} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('Notice test show false"', () => {
    const component = renderer.create(
        <Notice className="className" show={false} text="some text" agreeLabel="agree label" onAgree={() => {}} />
    );
    expect(component.toJSON()).toBe(null);
});
  
test('Notice test className"', () => {
    const component = shallow(
        <Notice className="className" show={false} text="some text" agreeLabel="agree label" onAgree={() => {}} />
    );
    expect(component.hasClass("className"));
});
  
test('Notice test text', () => {
    const component = shallow(
        <Notice className="className" show={true} text="some text" agreeLabel="agree label" onAgree={() => {}} />
    );
    expect(component.find("span").html()).toContain("some text");
});

test('Notice test button click', () => {
    const fn = jest.fn();
    const component = shallow(
        <Notice className="className" show={true} text="some text" agreeLabel="agree label" onAgree={fn} />
    );
    component.find('button').simulate('click');
    expect(fn.mock.calls.length).toBe(1);
});

test('ErrorPanel test button label', () => {
    const component = shallow(
        <Notice className="className" show={true} text="some text" agreeLabel="agree label" onAgree={() => {}} />
    );
    expect(component.find("button").text()).toEqual("agree label");
});

