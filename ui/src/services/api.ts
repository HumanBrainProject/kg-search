import { FetchBaseQueryError, createApi, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import sanitizeHtml from "sanitize-html";

import authConnector from "./authConnector";
import { SerializedError } from "@reduxjs/toolkit";

const regLegacyInstanceId = /^.+\/(.+)$/; //NOSONAR
const isMatchingLegacyInstanceId = (instanceId: string) => {
  return regLegacyInstanceId.test(instanceId);
};

interface QueryResultData {
  id?: string;
}

interface QueryArg {
  [key: string]: string;
}

const transformInstanceResponse = (data: QueryResultData, _meta: unknown, arg: QueryArg): unknown => {
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
      if (authConnector?.authAdapter?.tokenProvider?.token && !unauthenticatedEndpoints.includes(endpoint)) {
        headers.set("authorization", `Bearer ${authConnector.authAdapter.tokenProvider.token}`);
      }
      return headers;
    },
  }),
  tagTypes: tagTypes,
  endpoints: builder => ({
    getSettings: builder.query({
      //query: () => "../static/data/settings.json",
      query: () => "/settings",
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
      // query: () => "../static/data/instance.json",
      // transformResponse: (data, _meta, arg) => {
      //   data.id = arg.id;
      //   return data;
      // },
      query: ({ id, group }) => `/groups/${group}/documents/${id}`,
      //transformResponse: (data, meta, arg) => data,
      transformResponse: transformInstanceResponse,
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["Instance"],
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
      transformResponse: citation => citation?sanitizeHtml(citation as string, { allowedTags: [], allowedAttributes: {} }):"",
      keepUnusedDataFor: 1800, // 30 minutes cache
    }),
    getBibtex: builder.query({
      query: doi => ({
        url: `/citation?doi=${encodeURIComponent(doi)}&style=bibtex&contentType=application/x-bibtex`,
        responseHandler: "text"
      }),
      transformResponse: (bibtex: string) => window.URL.createObjectURL(new Blob([bibtex])),
      keepUnusedDataFor: 1800, // 30 minutes cache
    }),
    listFiles: builder.query({
      query: ({ repositoryId, group, groupingType, fileFormat }) => {
        let params = "";
        if (groupingType) {
          params += `${params.length?"&":"?"}groupingType=${encodeURIComponent(groupingType)}`;
        }
        if (fileFormat) {
          params += `${params.length?"&":"?"}format=${encodeURIComponent(fileFormat)}`;
        }
        return `/groups/${group}/repositories/${repositoryId}/files${params.length?params:""}`;
      },
      keepUnusedDataFor: 1800, // 30 minutes cache
      providesTags: ["Files"]
    }),
    listPreviewFiles: builder.query({
      query: ({ repositoryId, groupingType, fileFormat }) => {
        let params = "";
        if (groupingType) {
          params += `${params.length?"&":"?"}groupingType=${encodeURIComponent(groupingType)}`;
        }
        if (fileFormat) {
          params += `${params.length?"&":"?"}format=${encodeURIComponent(fileFormat)}`;
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

interface ErrorStatusText {
  [key:number] : string
}

const errorStatusText: ErrorStatusText = {
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

export const getError = (error?: FetchBaseQueryError|SerializedError|string) => {
  if (!error) {
    return "";
  }
  if (typeof error === "string") {
    return error;
  }
  let technicalError = "";
  if ("originalStatus" in error) {
    const statusText = errorStatusText[error.originalStatus];
    if (statusText) {
      technicalError = `${error.originalStatus} ${statusText}`;
    } else {
      technicalError = error.originalStatus as unknown as string;
    }
  } else if ("status" in error) {
    const code = Number(error.status);
    if (!isNaN(code) && errorStatusText[code]) {
      technicalError = `${error.status} ${errorStatusText[code]}`;
    } else {
      technicalError = error.status as string;
    }
  }
  if ("data" in error && error.data != technicalError && typeof error.data === "string" && error.data.indexOf("<") === -1) {
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