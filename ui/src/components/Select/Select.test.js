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
import Select from "./Select";

Enzyme.configure({ adapter: new Adapter() });

test("Select component renders initially", () => {
  const component = renderer.create(
    <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
  );

  expect(component.toJSON()).toMatchSnapshot();
});

test("Select test className", () => {
  const component = shallow(
    <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
  );
  expect(component.hasClass("className")).toBe(true);
});

test("Select test label", () => {
  const component = shallow(
    <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
  );
  expect(component.find("div div").text()).toBe("a label");
});

test("Select test number of items", () => {
  const component = render(
    <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={() => {}} />
  );
  expect(component.find("option").length).toBe(2);
});

test("Select test onchange", () => {
  const fn = jest.fn();
  const component = mount(
    <Select className="className" label="a label" list={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onChange={fn} />
  );
  component.find("select").simulate("change", {target: { value : "another value"}});
  expect(fn.mock.calls.length).toBe(1);
  expect(fn.mock.calls[0][0]).toBe("another value");
});