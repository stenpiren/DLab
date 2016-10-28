#!/usr/bin/python
import argparse
import json
from dlab.aws_actions import *
from dlab.aws_meta import *
import sys, os
import boto3, botocore


parser = argparse.ArgumentParser()
parser.add_argument('--vpc_id', type=str, default='')
parser.add_argument('--region', type=str, default='us-west-2')
parser.add_argument('--infra_tag_name', type=str, default='Name')
parser.add_argument('--infra_tag_value', type=str, default='BDCC-DSA-POC-infra')
args = parser.parse_args()


if __name__ == "__main__":
    success = False
    tag = {"Key": args.infra_tag_name, "Value": args.infra_tag_value}
    if args.vpc_id:
        print "Creating Endpoint in vpc {}, region {} with tag {}.".format(args.vpc_id, args.region, json.dumps(tag))
        ec2 = boto3.client('ec2')
        route_table = []
        endpoint = ''
        service_name = 'com.amazonaws.{}.s3'.format(args.region)
        print 'Vars are: {}, {}, {}'.format(args.vpc_id, service_name, json.dumps(tag))
        try:
            route_table = get_route_tables(args.vpc_id, json.dumps(tag))
            if not route_table:
                route_table.append(ec2.create_route_table(
                    VpcId = args.vpc_id
                )['RouteTable']['RouteTableId'])
                print 'Created Route-Table with ID: {}'.format(route_table)
                create_tag(route_table, json.dumps(tag))
            endpoints = get_vpc_endpoints(args.vpc_id)
            if not endpoints:
                print 'Creating EP'
                endpoint = ec2.create_vpc_endpoint(
                    VpcId=args.vpc_id,
                    ServiceName=service_name,
                    RouteTableIds=route_table
                    #   ClientToken='string'
                )
                endpoint = endpoint['VpcEndpoint']['VpcEndpointId']
            else:
                print 'For current VPC {} endpoint already exists. ID: {}. Route table list will be modified'.format(args.vpc_id, endpoints[0].get('VpcEndpointId'))
                endpoint_id = endpoints[0].get('VpcEndpointId')
                result = ec2.modify_vpc_endpoint(
                    VpcEndpointId=endpoint_id,
                    AddRouteTableIds=route_table
                )
                if result:
                    endpoint = endpoint_id
            print "ENDPOINT: " + endpoint
            success = True
        except botocore.exceptions.ClientError as err:
            print err.response['Error']['Message']
            print 'Failed to create endpoint. Removing RT'
            ec2.delete_route_table(
                RouteTableId=route_table[0]
            )
            success = False
    else:
        parser.print_help()
        sys.exit(2)

    if success:
        sys.exit(0)
    else:
        sys.exit(1)