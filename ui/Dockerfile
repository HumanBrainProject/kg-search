FROM node:16.13.2-alpine3.15 AS build-stage
RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . /usr/src/app
RUN npm install --force
RUN npm run build

FROM nginx:1.17.8-alpine
COPY --from=build-stage /usr/src/app/build /usr/share/nginx/html
CMD ["nginx", "-g", "daemon off;"]