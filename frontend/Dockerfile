FROM node:16.3 AS build

RUN mkdir /app
WORKDIR /app

COPY ./package*.json ./

RUN npm install

COPY . .
RUN npm run build

FROM nginx:latest AS web-server

COPY --from=build /app/dist/ /usr/share/nginx/html/
COPY ./nginx/default.conf /etc/nginx/conf.d/default.conf