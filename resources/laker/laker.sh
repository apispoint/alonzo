#!/bin/sh

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

AP_SERVICE_NAME=laker

region=$(wget -q -O - http://169.254.169.254/latest/meta-data/placement/availability-zone | sed 's/.$//')
instanceid=$(wget -q -O - http://169.254.169.254/latest/meta-data/instance-id)

#
# Disable cron service while we are currently caching
#
crontab -u root -l | grep -v '\*.*sh /opt/apispoint/laker/laker.sh' | crontab -u root -

while : ; do
    AP_LAKER_BUCKETS=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.laker:bucket*" | jq -r ".Tags[]|.Value")
    AP_LAKER_TARGET=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.laker:target" | jq -r ".Tags[]|.Value")
    [[ -n $AP_LAKER_BUCKETS && -n $AP_LAKER_TARGET ]] && break
    sleep 30
done

s3region=$(aws ec2 describe-tags --region $region --filters "Name=resource-id,Values=${instanceid}" "Name=key,Values=ap.laker:region" | jq -r ".Tags[]|.Value")
AP_LAKER_REGION=${s3region:-${region}}

for bucket in $AP_LAKER_BUCKETS; do
    status=`aws s3api wait bucket-exists --bucket $bucket > /dev/null 2>&1; echo $?`

    if [ "$status" -ne "0" ]; then
        aws s3api create-bucket --bucket $bucket --region $AP_LAKER_REGION > /dev/null 2>&1
    fi

    cache=$AP_LAKER_TARGET/$bucket

    mkdir -p $cache > /dev/null 2>&1
    aws s3 sync s3://$bucket $cache --region $AP_LAKER_REGION --delete --quiet
done

#
# re-enable cron service after caching is complete
#
(crontab -u root -l ; echo "*/1 * * * * sh /opt/apispoint/laker/laker.sh") | crontab -u root -
