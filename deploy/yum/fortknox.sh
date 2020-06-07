#!/bin/sh

export AP_HOME=/opt/apispoint
export AP_HOME_SERVICE=$AP_HOME/fortknox

sudo mkdir -p $AP_HOME_SERVICE
sudo aws s3 sync --quiet s3://ap-artifacts/LATEST/fortknox $AP_HOME_SERVICE --region us-east-1
sudo chmod u+x $AP_HOME_SERVICE/ftknox
sudo ln -s $AP_HOME_SERVICE/ftknox /usr/local/bin/ftknox