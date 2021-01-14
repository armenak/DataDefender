#!/bin/bash

docker run --network="host" -v $(pwd):/input --rm -it --name datadefender -v $(pwd):/input src_datadefender:latest /usr/local/datadefender/datadefender.docker "$@"
