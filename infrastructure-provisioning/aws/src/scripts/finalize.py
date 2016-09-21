#!/usr/bin/python
#  ============================================================================
# Copyright (c) 2016 EPAM Systems Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============================================================================
from time import gmtime, strftime
import boto3
import argparse

parser = argparse.ArgumentParser()
parser.add_argument('--key_id', type=str, default='')
args = parser.parse_args()


def cleanup(key_id):
    iam = boto3.resource('iam')
    current_user = iam.CurrentUser()
    for user_key in current_user.access_keys.all():
        if user_key.id == key_id:
            print "Deleted key " + user_key.id
            user_key.delete()

##############
# Run script #
##############

if __name__ == "__main__":
    cleanup(args.key_id)
