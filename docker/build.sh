#!/bin/bash

# To build application correctly launch this script from parent directory as ./docker/build.sh
mvn clean install && cp target/*.jar docker/app && cd docker && docker compose down && docker compose build && docker compose up