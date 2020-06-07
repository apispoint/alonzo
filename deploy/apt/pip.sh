#!/bin/sh

sudo apt-get -y update
sudo wget https://bootstrap.pypa.io/get-pip.py
sudo python get-pip.py
sudo rm get-pip.py