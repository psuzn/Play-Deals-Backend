set dotenv-load

default:
    @just --list

dev:
  ./gradlew backend:run
