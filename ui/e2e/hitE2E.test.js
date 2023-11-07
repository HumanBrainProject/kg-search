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

// eslint-disable-next-line @typescript-eslint/no-var-requires
const puppeteer = require('puppeteer');

describe('Hit Click opens the detail view E2E Test', () => {
  let browser;
  let page;

  // Launch a new browser instance before each test.
  beforeAll(async () => {
    browser = await puppeteer.launch();
    page = await browser.newPage();
  });

  // Close the browser instance after all tests are done.
  afterAll(async () => {
    await browser.close();
  });

  it('should open the detailed page', async () => {
    await page.goto('https://search.kg-ppd.ebrains.eu/search'); // Update the URL as needed.

    await page.waitForSelector('.kgs-hit-button');
    const buttons = await page.$$('.kgs-hit-button');  // Select all elements with the same class
    await buttons[0].click();  // Click the first button
    await page.waitForSelector('.kgs-carousel.kgs-detailView');
    const successMessage = await page.$('.kgs-carousel.kgs-detailView');
    expect(successMessage).not.toBeNull();
  });

});
