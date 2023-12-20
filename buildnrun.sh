mvn package
sudo docker build --no-cache -t spiderflow .
sudo docker run --rm -it --network="host" spiderflow