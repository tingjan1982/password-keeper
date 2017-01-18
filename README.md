[![Build Status](https://travis-ci.org/tingjan1982/password-keeper.svg?branch=master)](https://travis-ci.org/tingjan1982/password-keeper)

# password-keeper

> A REST API for managing passwords in a secured fashion. 2

## Features

* Dockerized container
* Automatic keystore location provisioning via EventListener.
    * external application logging (todo)
    * remote debugging (todo)
 
## Build Flow

This GitHub repository is configured with Travis CI integration. When changes are pushed to branch 'master', it will kick off a new Travis CI build
based on what's defined in .travis.yml. In a nutshell, the build will consist of:

1. Run maven build.
2. Create/overwrite a tag named 'master-snapshot'.
3. Create a GitHub release with the tag name in GitHub repository.
4. Trigger a DockerHub build to build the docker image and publish to DockerHub.
 
 
## Common Docker commands
 
### Build Image
```
sudo docker build -t joelin/passkeeper:latest . (assuming you are in the directory where Dockerfile is.)
```

### Run Image in a new Container (in background)
```
sudo docker run -d -p 8080:8080 joelin/passkeeper 
```

### Mount host volume in a new container
```
sudo docker run -d -p 8080:8080 -v /tmp/keystores:/tmp/keystores joelin/passkeeper
```

### Tail Logs 
```
sudo docker logs -f `sudo docker ps -f image=joelin/passkeeper -q`
```

### Stop Container by Image Name
```
sudo docker stop `sudo docker ps -f image=joelin/passkeeper -q`
```

## Reference

Volume API - http://stackoverflow.com/questions/18496940/how-to-deal-with-persistent-storage-e-g-databases-in-docker
Docker volume information - http://stackoverflow.com/questions/27977001/what-happen-to-docker-volume-on-deletion-of-container
