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
import ConditionalButton from "./ConditionalButton";

Enzyme.configure({ adapter: new Adapter() });

test('ConditionalButton component renders initially', () => {
    const component = renderer.create(
        <ConditionalButton className="test" test={true} onLabel="on" offLabel="off" onClick={() => {}} offClick={() => {}} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('ConditionalButton test className"', () => {
    const component = shallow(
        <ConditionalButton className="test" test={true} onLabel="on" offLabel="off" onClick={() => {}} offClick={() => {}} />
    );
    expect(component.hasClass("test"));
});
  
test('ConditionalButton test onLabel when test=true', () => {
    const component = shallow(
        <ConditionalButton className="test" test={true} onLabel="on" offLabel="off" onClick={() => {}} offClick={() => {}} />
    );
    expect(component.text()).toEqual('on');
});
  
test('ConditionalButton test offLabel when test=false', () => {
    const component = shallow(
        <ConditionalButton className="test" test={false} onLabel="on" offLabel="off" onClick={() => {}} offClick={() => {}} />
    );
    expect(component.text()).toEqual('off');
});
  
test('ConditionalButton onClick trigger onClick and not offClick when test=true', () => {
  const onFn = jest.fn();
  const offFn = jest.fn();
  const component = shallow(
    <ConditionalButton className="test" test={true} onLabel="on" offLabel="off" onClick={onFn} offClick={offFn} />
  );
  component.find('button').simulate('click');
  expect(onFn.mock.calls.length).toBe(1);
  expect(offFn.mock.calls.length).toBe(0);
});

test('ConditionalButton onClick trigger offClick and not onClick when test=true', () => {
    const onFn = jest.fn();
    const offFn = jest.fn();
    const component = shallow(
      <ConditionalButton className="test" test={false} onLabel="on" offLabel="off" onClick={onFn} offClick={offFn} />
    );
    component.find('button').simulate('click');
    expect(onFn.mock.calls.length).toBe(0);
    expect(offFn.mock.calls.length).toBe(1);
  });