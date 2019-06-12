#! /bin/sh

docker run -d -e PATH_BASE=/home/pipodi --network host --name=naspi pipodi/naspi
