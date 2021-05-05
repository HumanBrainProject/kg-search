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
import Enzyme, { shallow, render } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import ErrorPanel from "./ErrorPanel";

Enzyme.configure({ adapter: new Adapter() });

test('ErrorPanel component renders initially', () => {
    const component = renderer.create(
        <ErrorPanel show={true} message="some message" retryLabel="retry label" retryAction={{}} cancelLabel="cancel label" cancelAction={{}} onAction={() => {}} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('ErrorPanel test show false"', () => {
    const component = renderer.create(
        <ErrorPanel show={false} message="some message" retryLabel="retry label" retryAction={{}} cancelLabel="cancel label" cancelAction={{}} onAction={() => {}} />
    );
    expect(component.toJSON()).toBe(null);
});
  
test('ErrorPanel test message', () => {
    const component = shallow(
        <ErrorPanel show={true} message="some message" retryLabel="retry label" retryAction={{}} cancelLabel="cancel label" cancelAction={{}} onAction={() => {}} />
    );
    expect(component.find("span.kgs-error-message").text()).toEqual('some message');
});

test('ErrorPanel test retry button label', () => {
    const component = shallow(
        <ErrorPanel show={true} message="some message" retryLabel="retry label" retryAction={{}} cancelLabel="cancel label" cancelAction={{}} onAction={() => {}} />
    );
    expect(component.find("button").at(1).text()).toEqual("retry label");
});

test('ErrorPanel test cancel button label', () => {
    const component = shallow(
        <ErrorPanel show={true} message="some message" retryLabel="retry label" retryAction={{}} cancelLabel="cancel label" cancelAction={{}} onAction={() => {}} />
    );
    expect(component.find("button").at(0).text()).toEqual("cancel label");
});

test('ErrorPanel test retry button click', () => {
    const fn = jest.fn();
    const retryAction = {};
    const component = shallow(
        <ErrorPanel show={true} message="some message" retryLabel="retry label" retryAction={retryAction} cancelLabel="cancel label" cancelAction={{}} onAction={fn} />
    );
    component.find('button').at(1).simulate('click');
    expect(fn.mock.calls[0][0]).toBe(retryAction);
});

test('ErrorPanel test cancel button click', () => {
    const fn = jest.fn();
    const cancelAction = {};
    const component = shallow(
        <ErrorPanel show={true} message="some message" retryLabel="retry label" retryAction={{}} cancelLabel="cancel label" cancelAction={cancelAction} onAction={fn} />
    );
    component.find('button').at(0).simulate('click');
    expect(fn.mock.calls[0][0]).toBe(cancelAction);
});
