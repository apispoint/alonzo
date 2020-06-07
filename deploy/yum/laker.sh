#!/bin/sh

export AP_HOME=/opt/apispoint
export AP_HOME_SERVICE=$AP_HOME/laker

sudo mkdir -p $AP_HOME_SERVICE
sudo aws s3 sync --quiet s3://ap-artifacts/LATEST/laker $AP_HOME_SERVICE --region us-east-1
sudo chmod u+x $AP_HOME_SERVICE/laker.sh
sudo sh -c '(crontab -u root -l ; echo "@reboot sh /opt/apispoint/laker/laker.sh") | crontab -u root -'