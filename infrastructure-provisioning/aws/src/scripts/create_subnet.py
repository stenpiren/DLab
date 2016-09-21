#!/usr/bin/python
#  ============================================================================
# Copyright (c) 2016 EPAM Systems Inc. 
# 
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
# http://www.apache.org/licenses/LICENSE-2.0 
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License. 
# ============================================================================
import boto3
import sys
import argparse
import json

parser = argparse.ArgumentParser()
parser.add_argument('--vpc_id', type=str, default='')
parser.add_argument('--subnet', type=str, default='')
parser.add_argument('--region', type=str, default='us-west-2')
parser.add_argument('--infra_tag_name', type=str, default='BDCC-DSA-test-infra')
parser.add_argument('--infra_tag_value', type=str, default='tmp')
args = parser.parse_args()


def get_subnet_by_cidr(cidr):
    ec2 = boto3.resource('ec2')
    for subnet in ec2.subnets.filter(Filters=[{'Name': 'cidrBlock', 'Values': [cidr]}]):
        return subnet.id
    return ''


def create_subnet(vpc_id, subnet, tag):
    ec2 = boto3.resource('ec2')
    subnet = ec2.create_subnet(VpcId=vpc_id, CidrBlock=subnet)
    subnet.create_tags(Tags=[tag])
    return subnet.id


if __name__ == "__main__":
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.subnet != '':
        subnet_id = get_subnet_by_cidr(args.subnet)
        if subnet_id == '':
            print "Creating subnet %s in vpc %s, region %s with tag %s." % \
                  (args.subnet, args.vpc_id, args.region, json.dumps(tag))
            subnet_id = create_subnet(args.vpc_id, args.subnet, tag)
        else:
            print "REQUESTED SUBNET ALREADY EXISTS"
        print "SUBNET_ID " + subnet_id
    else:
        parser.print_help()
