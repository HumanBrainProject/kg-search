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

import React, { useEffect, useRef } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useLocation, useNavigate } from "react-router-dom";

import Instance from "../../features/instance/Instance";
import InstanceView from "./InstanceView";
import PreviousInstanceLinkButton from "../../components/PreviousInstanceLinkButton/PreviousInstanceLinkButton";
import Watermark from "../../components/Watermark/Watermark";
import Notification from "../../components/Notification/Notification";

import { setGroup } from "../../features/groups/groupsSlice";
import { requestInstance, syncHistory, selectPreviousInstance } from "../../features/instance/instanceSlice";

import { getUpdatedQuery, getLocationSearchFromQuery, searchToObj } from "../../helpers/BrowserHelpers";

import "./InstanceContainer.css";
import useScript from "../../app/hooks/useScript";

const InstanceContainer = ({ instanceId, path, isPreview, warning, watermark }) => {

  const initializedRef = useRef(null);

  const dispatch = useDispatch();

  const location = useLocation();
  const navigate = useNavigate();

  const data = useSelector(state => state.instance.data);
  const meta = useSelector(state => state.instance.meta);
  const title = useSelector(state => state.instance.title);
  const previousInstance = useSelector(state => selectPreviousInstance(state));
  const group = useSelector(state => state.groups.group);
  const defaultGroup = useSelector(state => state.groups.defaultGroup);

  useScript("application/ld+json", meta);

  useEffect(() => {
    if (!initializedRef.current !== instanceId) {
      initializedRef.current = instanceId;
      dispatch(requestInstance({
        instanceId: instanceId,
        context: location.state
      }));
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [instanceId, group]);

  useEffect(() => {
    const popstateHandler = () => {
      const reg = new RegExp(`^${path}(.+)$`);
      let previousGroup = searchToObj()["group"];
      if (!previousGroup) {
        previousGroup = defaultGroup;
      }
      const [, previousId] = reg.test(window.location.pathname) ? window.location.pathname.match(reg) : [null, null];
      dispatch(setGroup(previousGroup));
      dispatch(syncHistory(previousId));
    };
    window.addEventListener("popstate", popstateHandler, false);
    return () => {
      window.removeEventListener("popstate", popstateHandler);
    };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (instanceId) {
      document.title = `EBRAINS - ${title?title:instanceId}`;
    }
  }, [instanceId, title]);

  useEffect(() => {
    const query = getUpdatedQuery(searchToObj(), "group", group && group !== defaultGroup, group, false);
    const newLocationSearch = getLocationSearchFromQuery(query);
    if (newLocationSearch !== location.search) {
      navigate(`${location.pathname}${newLocationSearch}`);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [group]);

  return (
    <>
      <div className="kgs-instance-container" >
        <Notification text={warning} />
        {data && (
          <>
            {previousInstance && (
              <PreviousInstanceLinkButton title={previousInstance.title} />
            )}
            <InstanceView data={data} path={path} isSearch={false} />
          </>
        )}
        <Watermark text={watermark} />
      </div>
      <Instance isPreview={isPreview} isSearch={false} path={path} />
    </>
  );
};

export default InstanceContainer;