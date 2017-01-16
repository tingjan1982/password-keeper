[![Build Status](https://travis-ci.org/tingjan1982/password-keeper.svg?branch=master)](https://travis-ci.org/tingjan1982/password-keeper)

# password-keeper

## Features

* Automatic keystore location provisioning via EventListener.
* Dockerized container
 ** external application logging (todo)
 ** remote debugging (todo)
 
 
# Common Docker commands
 
## Build Image
 * sudo docker build -t joelin/passkeeper:latest . (assuming you are in the directory where Dockerfile is.)

## Run Image in a new Container (in background)
 * sudo docker run -d -p 8080:8080 joelin/passkeeper 

## Mount host volume in a new container (http://stackoverflow.com/questions/27977001/what-happen-to-docker-volume-on-deletion-of-container)
 * sudo docker run -d -p 8080:8080 -v /tmp/keystores:/tmp/keystores joelin/passkeeper
 
## Tail Logs 
 * sudo docker logs -f `sudo docker ps -f image=joelin/passkeeper -q`

## Stop Container by Image Name
 * sudo docker stop `sudo docker ps -f image=joelin/passkeeper -q`

# Reference

Volume API - http://stackoverflow.com/questions/18496940/how-to-deal-with-persistent-storage-e-g-databases-in-docker
