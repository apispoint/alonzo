#!/bin/sh

baseimg=${6:-baseimage.json}

pushd $REPO_HOME/ap.common.services/deploy/apt

~/dev/packer/packer build \
-var 'region='$1 \
-var 'ami-id='$2 \
-var 'role='$3 \
-var 'ssh-sg='$4 \
-var 'ssh-user='$5 $baseimg \

popd
