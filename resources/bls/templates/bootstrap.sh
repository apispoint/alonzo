#!/bin/bash

#
# Copyright APIS Point, LLC or its affiliates. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License").
# You may not use this file except in compliance with the License.
# A copy of the License is located at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
# or in the "license" file accompanying this file. This file is distributed
# on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
# express or implied. See the License for the specific language governing
# permissions and limitations under the License.
#

log() {
    LOG_MSG="ap.bls::bootstrap [${1}: ${3} (${2})]"
    echo $LOG_MSG
#    logger $LOG_MSG
}

region=$(wget -q -O - http://169.254.169.254/latest/meta-data/placement/availability-zone | sed 's/.$//')
instanceid=$(wget -q -O - http://169.254.169.254/latest/meta-data/instance-id)

while : ; do
    domain=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.bls:domain" | jq -r ".Tags[]|.Value")
    repository=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.bls:repository" | jq -r ".Tags[]|.Value")
    [[ -n $domain && -n $repository ]] && break
    sleep 1
done

s3region=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.bls:region" | jq -r ".Tags[]|.Value")
s3region=${s3region:-${region}}

repo='s3://'$repository

export AP_HOME=/opt/$domain
export AP_HOME_SERVICE=$AP_HOME/service

log 'INF' 'Loaded' 'AWS.REGION='"$region"
log 'INF' 'Loaded' 'AWS.INSTANCE_ID='"$instanceid"
log 'INF' 'Loaded' 'AP.BLS.DOMAIN='"$domain"
log 'INF' 'Loaded' 'AP.BLS.REPOSITORY='"$repository"
log 'INF' 'Loaded' 'AP.BLS.REGION='"$s3region"
log 'INF' 'Loaded' 'S3.REPO='"$repo"

services=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.bls:service*" | jq -r ".Tags[]|.Value")
for name in $services; do
    export SERVICE_HOME=$AP_HOME_SERVICE/$name
    rm -rf $SERVICE_HOME

    servicerepo=$repo/$name
    if [ $(aws s3 ls "${servicerepo}" > /dev/null 2>&1;echo $?) -eq 0 ]; then
        log 'INF' 'Loading' 'SERVICE='"${name}"

        mkdir -p $SERVICE_HOME
        aws s3 sync --quiet $servicerepo $SERVICE_HOME --region $s3region

        if [ -f $SERVICE_HOME/provision.sh ]
        then
            sh $SERVICE_HOME/provision.sh
        else
            cp $AP_HOME/bootstrap/service.sh.template $SERVICE_HOME/$name.sh
            cp $AP_HOME/bootstrap/systemd.service.template /etc/systemd/system/$name.service
            sed -i "s|{{name}}|$name|g; s|{{domain}}|$domain|g; s|{{servicerepo}}|$servicerepo|g" \
                $SERVICE_HOME/$name.sh \
                /etc/systemd/system/$name.service
            chmod u+x $SERVICE_HOME/$name.sh
            systemctl -q reenable $name;systemctl -q restart $name
        fi
    else
        log 'ERR' 'Not Found' 'SERVICE='"${name}"
    fi
done

# Executed only once at boot
systemctl stop bootstrap
#systemctl disable bootstrap
