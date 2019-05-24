#!/usr/bin/env bash

set -e

if [[ -n "$TRAVIS_TAG" ]]; then
    echo "Found tag: $TRAVIS_TAG pushing to docker hub a tagged image";
    docker tag pipodi/naspi pipodi/naspi:"$TRAVIS_TAG"
    docker push pipodi/naspi:"$TRAVIS_TAG";
else
    echo "Commit not tagged, will not push the image to docker hub";
fi;