# Play Deals

[![Static Badge](https://img.shields.io/badge/Android-black?logo=android&logoColor=white&color=%234889f5)](https://play.google.com/store/apps/details?id=me.sujanpoudel.playdeals)
&nbsp;
[![Static Badge](https://img.shields.io/badge/IOS-grey?logo=apple)](https://github.com/psuzn/app-deals/releases/latest)
&nbsp;&nbsp;
[![Static Badge](https://img.shields.io/badge/macOS-black?logo=apple)](https://github.com/psuzn/app-deals/releases/latest)
&nbsp;
[![Static Badge](https://img.shields.io/badge/Windows-green?logo=windows&color=blue)](https://github.com/psuzn/app-deals/releases/latest)
&nbsp;
[![Static Badge](https://img.shields.io/badge/Linux-white?logo=linux&logoColor=white&color=grey)](https://github.com/psuzn/app-deals/releases/latest)
&nbsp;

![Static Badge](https://img.shields.io/badge/License-GPL--v3-brightgreen)
[![play-deals-backend 1.0 CI](https://github.com/psuzn/play-deals-backend/actions/workflows/ci.yaml/badge.svg)](https://github.com/psuzn/play-deals-backend/actions/workflows/ci.yaml)

![Feature](./media/feature-graphic.jpeg)

| <a href="https://play.google.com/store/apps/details?id=me.sujanpoudel.playdeals">     <img src="media/badge-get-on-google-play.png" width="200" alt="Get it on Google play">   </a> | <a href="https://github.com/psuzn/app-deals/releases/latest">     <img src="media/badge-download-apk.png" width="160" alt="Download Apk">   </a> |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------:|

Play deals is a simple app that aggregates the paid apps that have ongoing deals and discounts, aka you can get the
paid apps free or with discount.

# Play Deals

Play deals is a simple app to aggregate the paid apps that have ongoing deals and discounts.

This repo contains the codebase for the backend for the actual app. It acts as a place to persist the deals and a
place to add,validate the deals. This backend is built using [Eclipse Vert.x™](https://vertx.io/) and heavily makes use
of kotlin coroutines.

## Development

### Running tests

```shell
./gradlew test
```

### Configuration

Configuration can be done by passing environment variables listed below:

> Best way provide configuration to create a `.env` file with the environmental variables and either
> 1. run `just dev-run` from terminal, OR
> 2. Install [Envfile](https://plugins.jetbrains.com/plugin/7861-envfile) plugin for IntelliJ and run using IntelliJ

| ENV_VAR                           | REQUIRED | DEFAULT       | EXAMPLE      | NOTES                                                                  |
|-----------------------------------|----------|---------------|--------------|:-----------------------------------------------------------------------|
| `DB_HOST`                         | `Y`      |               | `localhost`  |                                                                        |
| `DB_USERNAME`                     | `Y`      |               | `whatever`   |                                                                        |
| `FIREBASE_ADMIN_AUTH_CREDENTIALS` | `Y`      |               | `whatever`   | Firebase admin auth credentials                                        |
| `FOREX_API_KEY`                   | `Y`      |               | `whatever`   | Api key for [https://exchangeratesapi.io](https://exchangeratesapi.io) |
| `DB_PASSWORD`                     | `N`      | `password`    | `whatever`   |                                                                        |
| `DB_PORT`                         | `N`      | `5432`        | `6868`       |                                                                        |
| `DB_NAME`                         | `N`      | `play_deals`  | `whatever`   |                                                                        |
| `DB_POOL_SIZE`                    | `N`      | `5`           | `6`          |                                                                        |
| `ENV`                             | `N`      | `PRODUCTION`  | `PRODUCTION` | one of `PRODUCTION or DEVELOPMENT or TEST `                            |
| `APP_PORT`                        | `N`      | `8888`        | `9999`       |                                                                        |
| `POSTGRES_IMAGE`                  | `N`      | `postgres:14` |              | Useful for testing new versions of postgres. Used only in test code    |
| `DASHBOARD`                       | `N`      | `true`        | `false`      | Whether to enable or not the Jobrunr dashboard                         |
| `DASHBOARD_USER`                  | `N`      | `admin`       | `whatever`   | Jobrunr dashboard login credential                                     |
| `DASHBOARD_PASS`                  | `N`      | `admin`       | `whatever`   | Jobrunr dashboard login credential                                     |
| `CORS`                            | `N`      | `*`           | `whatever`   | origins allowed for CORS                                               |

## License

**GPL V3 License**

Copyright (c) 2023 Sujan Poudel
