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
import React, { Suspense } from "react";
import { useSelector, useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import { reset } from "../../../features/instance/instanceSlice";

import ShareButtons from "../../../features/ShareButtons";
import InstanceView from "../../Instance/InstanceView";
import FetchingPanel from "../../../components/FetchingPanel/FetchingPanel";

const Carousel = React.lazy(() => import("../../../components/Carousel/Carousel"));

import "./DetailView.css";

const itemComponent = ({ data, customNavigationComponent }) => <InstanceView data={data} path="/instances/" isSearch={true} customNavigationComponent={customNavigationComponent} />;

const DetailView = () => {

  const navigate = useNavigate();

  const dispatch = useDispatch();

  const current = useSelector(state => state.instance.data);
  const history = useSelector(state => state.instance.history);

  const handleOnBack = () => navigate(-1);

  const handleOnClose = () => {
    dispatch(reset());
  };

  if (current) {

    const data = [
      ...history.map(() => null),
      current
    ];

    return (
      <Suspense fallback={<FetchingPanel message="Loading resource..." />}>
        <Carousel className="kgs-detailView" data={data} itemComponent={itemComponent} navigationComponent={ShareButtons} onBack={handleOnBack} onClose={handleOnClose} />
      </Suspense>
    );
  }

  return null;
};

export default DetailView;