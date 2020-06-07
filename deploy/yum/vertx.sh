#!/bin/sh

export AP_HOME=/opt/apispoint

sudo mkdir -p $AP_HOME
sudo wget https://bintray.com/artifact/download/vertx/downloads/vert.x-3.9.1-full.tar.gz -O $AP_HOME/vertx.tar.gz
sudo tar zxf $AP_HOME/vertx.tar.gz -C $AP_HOME
sudo rm $AP_HOME/vertx.tar.gz