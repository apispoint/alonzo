#!/bin/sh

export AP_HOME=/opt/apispoint
export AP_HOME_SERVICE=$AP_HOME/bootstrap

sudo mkdir -p $AP_HOME_SERVICE
sudo aws s3 sync --quiet s3://ap-artifacts/LATEST/bootstrap $AP_HOME_SERVICE --region us-east-1
sudo chmod u+x $AP_HOME_SERVICE/bootstrap.sh
sudo cp $AP_HOME_SERVICE/bootstrap.service /etc/systemd/system
sudo systemctl enable bootstrap