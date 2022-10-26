import { createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import sanitizeHtml from "sanitize-html";

import Sentry from "./sentry";
import Matomo from "./matomo";
import { getAccessToken } from "../../features/auth/authSlice";

const regLegacyInstanceId = /^.+\/(.+)$/; //NOSONAR
const isMatchingLegacyInstanceId = (instanceId) => {
  return regLegacyInstanceId.test(instanceId);
};

const transformInstanceResponse = (data, _meta, arg) => {
  const id = arg?.id;
  if (id && isMatchingLegacyInstanceId(id)) {
    data.id = id;
  }
  return data;
};

const unauthenticatedEndpoints = ["getSettings", "getCitation", "getBibtex"];

const tagTypes = ["Group", "Search", "Instance", "Preview", "Files", "PreviewFiles", "Format", "PreviewFormat", "GroupingType", "PreviewGroupingType", "LinkedInstance", "LinkedPreview"];

export const tagsToInvalidateOnLogout = tagTypes.map(tag => ({ type: tag, id: "LIST" }));

export const api = createApi({
  reducerPath: "api",
  baseQuery: fetchBaseQuery({
    baseUrl: "/api",
    //prepareHeaders: (headers, { endpoint, getState }) => {
    prepareHeaders: (headers, { endpoint }) => {
      //const state = getState();
      const token = getAccessToken();
      if (token && !unauthenticatedEndpoints.includes(endpoint)) {
        headers.set("authorization", `Bearer ${token}`);
      }
      return headers;
    },
  }),
  tagTypes: ["Group", "Search", "Instance", "Preview", "Files", "PreviewFiles", "Format", "PreviewFormat", "GroupingType", "PreviewGroupingType", "LinkedInstance", "LinkedPreview"],
  endpoints: builder => ({
    getSettings: builder.query({
      query: () => "../static/data/settings.json",
      // query: () => "/settings",
      //async onQueryStarted(arg, {dispatch, getState, extra, requestId, queryFulfilled}) {
      async onQueryStarted(_arg, { queryFulfilled }) {
        try {
          const { data } = await queryFulfilled; // { data, meta }
          const isLocalDev = window.location.host.startsWith("localhost");
          Sentry.initialize(isLocalDev?null:data?.sentry);
          Matomo.initialize(isLocalDev?null:data?.matomo);
        } catch (e) {
          //console.log(e);
        }
      }
    }),
    listGroups: builder.query({
      query: () => "/groups",
      providesTags: ["Group"]
    }),
    getSearch: builder.query({
      //query: () => "../static/data/search.json",
      query: ({ group, q, type, from, size, payload }) => ({
        url: `/groups/${group}/search?${q?("q=" + encodeURIComponent(q) + "&"):""}type=${encodeURIComponent(type)}&from=${from}&size=${size}`,
        method: "POST",
        body: payload
      }),
      keepUnusedDataFor: 600, // 10 minutes cache
      providesTags: ["Search"]
    }),
    getInstance: builder.query({
      query: () => "../static/data/instance.json",
      transformResponse: (data, _meta, arg) => {
        data.id = arg.id;
        return data;
      },
      // query: ({ id, group }) => `/groups/${group}/documents/${id}`,
      //transformResponse: (data, meta, arg) => data,
      // transformResponse: transformInstanceResponse,
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["Instance"]
    }),
    getPreview: builder.query({
      //query: () => "../static/data/instance.json",
      query: id => `${id}/live?skipReferenceCheck=true`,
      keepUnusedDataFor: 0.0001, // no cache for live
      providesTags: ["Preview"]
    }),
    getCitation: builder.query({
      query: doi => ({
        url: `/citation?doi=${encodeURIComponent(doi)}&style=apa&contentType=text/x-bibliography`,
        responseHandler: "text"
      }),
      transformResponse: citation => sanitizeHtml(citation, { allowedTags: [], allowedAttributes: {} }),
      keepUnusedDataFor: 1800, // 30 minutes cache
    }),
    getBibtex: builder.query({
      query: doi => ({
        url: `/citation?doi=${encodeURIComponent(doi)}&style=bibtex&contentType=application/x-bibtex`,
        responseHandler: "text"
      }),
      transformResponse: bibtex => window.URL.createObjectURL(new Blob([bibtex])),
      keepUnusedDataFor: 1800, // 30 minutes cache
    }),
    listFiles: builder.query({
      query: ({ repositoryId, group, searchAfter, groupingType, fileFormat, size }) => {
        let params = "";
        if (searchAfter) {
          params += `?searchAfter=${encodeURIComponent(searchAfter)}`;
        }
        if (groupingType) {
          params += `${params.length?"&":"?"}groupingType=${encodeURIComponent(groupingType)}`;
        }
        if (fileFormat) {
          params += `${params.length?"&":"?"}format=${encodeURIComponent(fileFormat)}`;
        }
        if (size) {
          params += `${params.length?"&":"?"}size=${size}`;
        }
        return `/groups/${group}/repositories/${repositoryId}/files${params.length?params:""}`;
      },
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["Files"]
    }),
    listPreviewFiles: builder.query({
      query: ({ repositoryId, searchAfter, groupingType, fileFormat, size }) => {
        let params = "";
        if (searchAfter) {
          params += `?searchAfter=${encodeURIComponent(searchAfter)}`;
        }
        if (groupingType) {
          params += `${params.length?"&":"?"}groupingType=${encodeURIComponent(groupingType)}`;
        }
        if (fileFormat) {
          params += `${params.length?"&":"?"}format=${encodeURIComponent(fileFormat)}`;
        }
        if (size) {
          params += `${params.length?"&":"?"}size=${size}`;
        }
        return `/repositories/${repositoryId}/files/live${params.length?params:""}`;
      },
      keepUnusedDataFor: 0.0001, // no cache for live
      providesTags: ["PreviewFiles"]
    }),
    listFormats: builder.query({
      query: ({ repositoryId, group }) => `/groups/${group}/repositories/${repositoryId}/files/formats`,
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["Format"]
    }),
    listPreviewFormats: builder.query({
      query: repositoryId => `/repositories/${repositoryId}/files/formats/live`,
      keepUnusedDataFor: 0.0001, // no cache for live
      providesTags: ["PreviewFormat"]
    }),
    listGroupingTypes: builder.query({
      query: ({ repositoryId, group }) => `/groups/${group}/repositories/${repositoryId}/files/groupingTypes`,
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["GroupingType"]
    }),
    listPreviewGroupingTypes: builder.query({
      query: repositoryId => `/repositories/${repositoryId}/files/groupingTypes/live`,
      keepUnusedDataFor: 0.0001, // no cache for live
      providesTags: ["PreviewGroupingType"]
    }),
    getLinkedInstance: builder.query({
      //query: () => "/static/data/instance.json",
      query: ({ id, group }) => `/groups/${group}/documents/${id}`,
      //transformResponse: (data, meta, arg) => data,
      transformResponse: transformInstanceResponse,
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["LinkedInstance"]
    }),
    getLinkedPreview: builder.query({
      //query: () => "/static/data/instance.json",
      query: id => `${id}/live?skipReferenceCheck=true`,
      keepUnusedDataFor: 0.0001, // no cache for live
      providesTags: ["LinkedPreview"]
    }),
  })
});

export const {
  useGetSettingsQuery,
  useListGroupsQuery,
  useGetSearchQuery,
  useGetInstanceQuery,
  useGetPreviewQuery,
  useGetCitationQuery,
  useGetBibtexQuery,
  useListFilesQuery,
  useListPreviewFilesQuery,
  useListFormatsQuery,
  useListPreviewFormatsQuery,
  useListGroupingTypesQuery,
  useListPreviewGroupingTypesQuery,
  useGetLinkedInstanceQuery,
  useGetLinkedPreviewQuery,
} = api;

export const captureException = e => Sentry.captureException(e);

export const showReportDialog = customSettings => Sentry.showReportDialog(customSettings);

export const setCustomUrl = url => Matomo.setCustomUrl(url);

export const trackPageView = () => Matomo.trackPageView();

export const trackEvent = (category, name, value) => Matomo.trackEvent(category, name, value);

export const trackLink = (category, name) => Matomo.trackLink(category, name);

const errorStatusText = {
  400: "Bad Request",
  401: "Unauthorized",
  403: "Forbidden",
  404: "Not Found",
  405: "Method Not Allowed",
  406: "Not Acceptable",
  407: "Proxy Authentication Required",
  408: "Request Timeout",
  409: "Conflict",
  410: "Gone",
  411: "Length Required",
  412: "Precondition Failed",
  413: "Payload Too Large",
  414: "URI Too Long",
  415: "Unsupported Media Type",
  416: "Range Not Satisfiable",
  417: "Expectation Failed",
  421: "Misdirected Request",
  425: "Too Early",
  426: "Upgrade Required",
  428: "Precondition Required",
  429: "Too Many Requests",
  431: "Request Header Fields Too Large",
  451: "Unavailable For Legal Reasons",
  500: "Internal Server Error",
  501: "Not Implemented",
  502: "Bad Gateway",
  503: "Service Unavailable",
  504: "Gateway Timeout",
  505: "HTTP Version Not Supported",
  506: "Variant Also Negotiates",
  510: "Not Extended",
  511: "Network Authentication Required",
  420: "Method Failure",
  598: "Network read timeout",
  599: "Network Connect Timeout",
  440: "Login Time-out",
  444: "No Response",
  494: "Request header too large",
  495: "SSL Certificate Error",
  496: "SSL Certificate Required",
  497: "HTTP Request Sent to HTTPS Port",
  499: "Client Closed Request"
};

export const getError = error => {
  let technicalError = "";
  if (error?.originalStatus) {
    const statusText = errorStatusText[error.originalStatus];
    if (statusText) {
      technicalError = `${error.originalStatus} ${statusText}`;
    } else {
      technicalError = error.originalStatus;
    }
  } else if (error.status) {
    const code = Number(error.status);
    if (!isNaN(code) && errorStatusText[code]) {
      technicalError = `${error.status} ${errorStatusText[code]}`;
    } else {
      technicalError = error.status;
    }
  }
  if (error.data && error.data != technicalError && typeof error.data === "string" && error.data.indexOf("<") === -1) {
    if (technicalError) {
      technicalError += ": ";
    }
    technicalError += error.data;
  }
  let message = "The service is temporary unavailable. Please retry in a moment.";
  if (technicalError) {
    message += `(${technicalError})`;
  }
  return message;
};