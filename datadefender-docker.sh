#!/bin/bash

docker run --network="host" -v $(pwd):/input --rm -it --name datadefender -v $(pwd):/input datadefender_datadefender /usr/local/datadefender/datadefender.docker "$@"
