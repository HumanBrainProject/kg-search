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
import CopyToClipboardButton from "./CopyToClipboardButton";

Enzyme.configure({ adapter: new Adapter() });

test('CopyToClipboardButton component renders initially', () => {
    const component = renderer.create(
        <CopyToClipboardButton className="className" text="text" icon="iconClassName" title="title" confirmationText="confirmation text" content="content" />
    );
  
    expect(component.toJSON()).toMatchSnapshot();
});

test('CopyToClipboardButton test className"', () => {
    const component = shallow(
        <CopyToClipboardButton className="className" text="text" icon="iconClassName" title="title" confirmationText="confirmation text" content="content" />
    );
    expect(component.hasClass("className"));
});
  
test('CopyToClipboardButton test icon"', () => {
    const component = shallow(
        <CopyToClipboardButton className="className" text="text" icon="iconClassName" title="title" confirmationText="confirmation text" content="content" />
    );
    expect(component.find("i").hasClass("iconClassName"));
});
  
test('CopyToClipboardButton test title', () => {
    const component = shallow(
        <CopyToClipboardButton className="className" text="text" icon="iconClassName" title="title" confirmationText="confirmation text" content="content" />
    );
    expect(component.find("button").prop("title")).toEqual('title');
});
  
test('CopyToClipboardButton test text', () => {
    const component = shallow(
        <CopyToClipboardButton className="className" text="text" icon="iconClassName" title="title" confirmationText="confirmation text" content="content" />
    );
    expect(component.find("button span").text()).toEqual('text');
});
  
test('CopyToClipboardButton test confirmationText', () => {
    const component = shallow(
        <CopyToClipboardButton className="className" text="text" icon="iconClassName" title="title" confirmationText="confirmation text" content="content" />
    );
    expect(component.find("div").text()).toEqual('confirmation text');
});