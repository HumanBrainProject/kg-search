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
import Enzyme, { shallow } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import EmailToLink from "./EmailToLink";

Enzyme.configure({ adapter: new Adapter() });

test("EmailToLink component renders initially", () => {
  const component = renderer.create(
    <EmailToLink className="test" title="title" text="text" link="link" icon="times" />
  );

  expect(component.toJSON()).toMatchSnapshot();
});

test("EmailToLink test className\"", () => {
  const component = shallow(
    <EmailToLink className="test" title="title" text="text" link="link" icon="times" />
  );
  expect(component.hasClass("test")).toBe(true);
});

test("EmailToLink test text", () => {
  const component = shallow(
    <EmailToLink className="test" title="title" text="text" link="link" icon="times" />
  );
  expect(component.text()).toContain("text");
});

test("EmailToLink test title", () => {
  const component = shallow(
    <EmailToLink className="test" title="title" text="text" link="link" icon="times" />
  );
  expect(component.prop("title")).toEqual("title");
});

test("EmailToLink test icon", () => {
  const component = shallow(
    <EmailToLink className="test" title="title" text="text" link="link" icon="times" />
  );
  expect(component.find("svg")).toBe(true);
});