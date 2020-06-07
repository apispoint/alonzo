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

echo "----------------------------------------"
echo "---" Avant Garde Core Services
echo "----------------------------------------"

##############################################
# Ready the domain repository
##############################################

echo "****************************************"
echo "***" Readying $repository
echo "****************************************"

$AWS_HOME/bin/aws s3 mb s3://$repository --region $region

##############################################
# Bootstrap Loading Service aka BLiS
##############################################

name=bootstrap

echo "****************************************"
echo "***" $name
echo "****************************************"

BLS_DIR=$REPO_HOME/ap.common.services/resources/bls

sed 's/{{name}}/'$name'/g; s/{{domain}}/'$domain'/g;' $BLS_DIR/templates/$name.service.template > $BLS_DIR/templates/$name.service

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region
$AWS_HOME/bin/aws s3 cp $BLS_DIR/templates s3://$repository/$version/$name/ --recursive --region $region

rm $BLS_DIR/templates/$name.service

##############################################
# Fort Knox Credential Storage
##############################################

name=fortknox

echo "****************************************"
echo "***" $name
echo "****************************************"

FTKNOX_DIR=$REPO_HOME/ap.common.services/resources/fortknox

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region
$AWS_HOME/bin/aws s3 cp $FTKNOX_DIR s3://$repository/$version/$name/ --recursive --region $region

##############################################
# Laker Storage Sync
##############################################

name=laker

echo "****************************************"
echo "***" $name
echo "****************************************"

LAKER_DIR=$REPO_HOME/ap.common.services/resources/laker

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region
$AWS_HOME/bin/aws s3 cp $LAKER_DIR s3://$repository/$version/$name/ --recursive --region $region

##############################################
# Build Avant Garde Core Services
##############################################

PROJECT=$REPO_HOME/ap.common.services
RESOURCES=$PROJECT/resources
BUILD=$PROJECT/build/libs

$GRADLE_HOME/bin/gradle -b $PROJECT/build.gradle clean build copyToLibs

##############################################
# Timezone Service
##############################################

name=timezone

echo "****************************************"
echo "***" $name
echo "****************************************"

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region

$AWS_HOME/bin/aws s3 cp $BUILD s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include "bc*fips*.jar" \
    --include "java-jwt*.jar" \
    --include "jackson-core*.jar" \
    --include "jackson-annotations*.jar" \
    --include "jackson-databind*.jar" \
    --include "jackson-dataformat-cbor*.jar" \
    --include "ap.common.services*.jar" \
    --include "mjson-*.jar"

$AWS_HOME/bin/aws s3 cp $RESOURCES/configs s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include $name".config.json"

##############################################
# CountryCodes Service
##############################################

name=countrycode

echo "****************************************"
echo "***" $name
echo "****************************************"

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region

$AWS_HOME/bin/aws s3 cp $BUILD s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include "bc*fips*.jar" \
    --include "java-jwt*.jar" \
    --include "jackson-core*.jar" \
    --include "jackson-annotations*.jar" \
    --include "jackson-databind*.jar" \
    --include "jackson-dataformat-cbor*.jar" \
    --include "ap.common.services*.jar" \
    --include "mjson-*.jar"

$AWS_HOME/bin/aws s3 cp $RESOURCES/configs s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include $name".config.json"

##############################################
# Signin Service
##############################################

name=signin

echo "****************************************"
echo "***" $name
echo "****************************************"

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region

$AWS_HOME/bin/aws s3 cp $BUILD s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include "aws-java-sdk-dynamodb*.jar" \
    --include "aws-java-sdk-kms*.jar" \
    --include "aws-java-sdk-s3*.jar" \
    --include "aws-java-sdk-ses-*.jar" \
    --include "aws-java-sdk-core*.jar" \
    --include "bc*fips*.jar" \
    --include "java-jwt*.jar" \
    --include "jackson-core*.jar" \
    --include "jackson-annotations*.jar" \
    --include "jackson-databind*.jar" \
    --include "jackson-dataformat-cbor*.jar" \
    --include "commons-logging*.jar" \
    --include "httpclient*.jar" \
    --include "httpcore*.jar" \
    --include "joda-time*.jar" \
    --include "ap.common.services*.jar" \
    --include "mjson-*.jar" \
    --include "core*.jar" \
    --include "javase*.jar"

$AWS_HOME/bin/aws s3 cp $RESOURCES/configs s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include $name".config.json"

$AWS_HOME/bin/aws s3 cp $RESOURCES/schemas s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include $name"-schema.json" \

$AWS_HOME/bin/aws s3 cp $INIT_AUTH_PASSWD s3://$repository/$version/$name/ --region $region

##############################################
# User Service
##############################################

name=user

echo "****************************************"
echo "***" $name
echo "****************************************"

$AWS_HOME/bin/aws s3 rm s3://$repository/$version/$name --recursive --region $region

$AWS_HOME/bin/aws s3 cp $BUILD s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include "aws-java-sdk-dynamodb*.jar" \
    --include "aws-java-sdk-kms*.jar" \
    --include "aws-java-sdk-s3*.jar" \
    --include "aws-java-sdk-ses-*.jar" \
    --include "aws-java-sdk-core*.jar" \
    --include "bc*fips*.jar" \
    --include "java-jwt*.jar" \
    --include "jackson-core*.jar" \
    --include "jackson-annotations*.jar" \
    --include "jackson-databind*.jar" \
    --include "jackson-dataformat-cbor*.jar" \
    --include "commons-logging*.jar" \
    --include "httpclient*.jar" \
    --include "httpcore*.jar" \
    --include "joda-time*.jar" \
    --include "ap.common.services*.jar" \
    --include "mjson-*.jar"

$AWS_HOME/bin/aws s3 cp $RESOURCES/configs s3://$repository/$version/$name/ --region $region --recursive --exclude "*" \
    --include $name".config.json"

$AWS_HOME/bin/aws s3 cp $INIT_AUTH_PASSWD s3://$repository/$version/$name/ --region $region
