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
import Enzyme, { mount, shallow, render } from "enzyme";
import Adapter from "enzyme-adapter-react-16";
import Carrousel from "./Carrousel";

Enzyme.configure({ adapter: new Adapter() });

test('Carrousel component renders initially', () => {
    const component = renderer.create(
        <Carrousel className="className" show={true} value="a value" data={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onPrevious={() => {}} onClose={() => {}} itemComponent={() => null} navigationComponent={() => null} cookielawBanner={() => null} noticeComponent={() => null} />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('Carrousel test show false"', () => {
    const component = renderer.create(
        <Carrousel className="className" show={false} value="a value" data={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onPrevious={() => {}} onClose={() => {}} itemComponent={() => null} navigationComponent={() => null} cookielawBanner={() => null} noticeComponent={() => null} />
    );
    expect(component.toJSON()).toBe(null);
});
  
test('Carrousel test className"', () => {
    const component = shallow(
        <Carrousel className="className" show={true} value="a value" data={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onPrevious={() => {}} onClose={() => {}} itemComponent={() => null} navigationComponent={() => null} cookielawBanner={() => null} noticeComponent={() => null} />
    );
    expect(component.hasClass("className"));
});

test('Carrousel test number of items', () => {
    const component = render(
        <Carrousel className="className" show={true} value="a value" data={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onPrevious={() => {}} onClose={() => {}} itemComponent={() => null} navigationComponent={() => null} cookielawBanner={() => null} noticeComponent={() => null} />
    );
    expect(component.find(".kgs-carrousel__item").length).toBe(5);
});

test('Carrousel test items', () => {
    const component = renderer.create(
        <Carrousel className="className" show={true} value="a value" data={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onPrevious={() => {}} onClose={() => {}} itemComponent={() => null} navigationComponent={() => null} cookielawBanner={() => null} noticeComponent={() => null} />
    );
    const instance = component.getInstance();

    expect(instance.items.length).toBe(5);
    expect(instance.items[1].position).toBe(0);
    expect(instance.items[1].isActive);
    expect(instance.items[1].data.label).toBe("another label");
});

test('Carrousel test navigation buttons', () => {
    const previousFn = jest.fn();
    const closeFn = jest.fn();
    const component = mount(
        <Carrousel className="className" show={true} value="a value" data={[{label: "a label", value: "a value"},{label: "another label", value: "another value"}]} onPrevious={previousFn} onClose={closeFn} itemComponent={() => null} navigationComponent={() => null} cookielawBanner={() => null} noticeComponent={() => null} />
    );
    expect(component.find(".kgs-carrousel__previous-button").length).toBe(1);
    expect(component.find(".kgs-carrousel__close-button").length).toBe(1);
    component.find('.kgs-carrousel__previous-button').simulate('click');
    expect(previousFn.mock.calls.length).toBe(1);
    expect(closeFn.mock.calls.length).toBe(0);
    component.find('.kgs-carrousel__close-button').simulate('click');
    expect(previousFn.mock.calls.length).toBe(1);
    expect(closeFn.mock.calls.length).toBe(1);
});
