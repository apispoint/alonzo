#!/bin/sh

export AP_HOME=/opt/apispoint
export AP_ES_INSTALL=$AP_HOME/es_tmp

sudo mkdir -p $AP_ES_INSTALL
sudo wget https://download.elasticsearch.org/elasticsearch/release/org/elasticsearch/distribution/tar/elasticsearch/2.2.1/elasticsearch-2.2.1.tar.gz -O $AP_ES_INSTALL/elasticsearch.tar.gz
sudo tar zxf $AP_ES_INSTALL/elasticsearch.tar.gz -C $AP_HOME
sudo mv $AP_HOME/elasticsearch-* $AP_HOME/elasticsearch
sudo rm $AP_ES_INSTALL/elasticsearch.tar.gz
sudo $AP_HOME/elasticsearch/bin/plugin -install royrusso/elasticsearch-HQ
sudo $AP_HOME/elasticsearch/bin/plugin -install cloud-aws
sudo $AP_HOME/elasticsearch/bin/plugin -install lukas-vlcek/bigdesk
echo 'echo -e "`(curl http://169.254.169.254/latest/meta-data/local-ipv4)` $HOSTNAME" >> /etc/hosts' | sudo -s
echo 'echo -e "discovery.zen.ping.multicast.enabled: false" >> /opt/apispoint/elasticsearch/config/elasticsearch.yml' | sudo -s
echo 'echo -e "discovery.type: ec2" >> /opt/apispoint/elasticsearch/config/elasticsearch.yml' | sudo -s
echo 'echo -e "discovery.ec2.tag.Why: MDC" >> /opt/apispoint/elasticsearch/config/elasticsearch.yml' | sudo -s
echo 'echo -e "cluster.name: MDCCluster" >> /opt/apispoint/elasticsearch/config/elasticsearch.yml' | sudo -s
sudo rm -r $AP_ES_INSTALL