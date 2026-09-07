package com.pulumi.webserverfleet;

import com.pulumi.Pulumi;
import com.pulumi.aws.ec2.Vpc;
import com.pulumi.aws.ec2.VpcArgs;
import com.pulumi.aws.ec2.Subnet;
import com.pulumi.aws.ec2.SubnetArgs;
import com.pulumi.aws.ec2.InternetGateway;
import com.pulumi.aws.ec2.InternetGatewayArgs;
import com.pulumi.aws.ec2.RouteTable;
import com.pulumi.aws.ec2.RouteTableArgs;
import com.pulumi.aws.ec2.Route;
import com.pulumi.aws.ec2.RouteArgs;
import com.pulumi.aws.ec2.RouteTableAssociation;
import com.pulumi.aws.ec2.RouteTableAssociationArgs;
import com.pulumi.aws.ec2.SecurityGroup;
import com.pulumi.aws.ec2.SecurityGroupArgs;
import com.pulumi.aws.ec2.inputs.SecurityGroupIngressArgs;
import com.pulumi.webserverfleet.WebServerFleet;
import com.pulumi.webserverfleet.WebServerFleetArgs;   
import com.pulumi.core.Output;
import com.pulumi.aws.ec2.Instance; 
import com.pulumi.aws.ec2.inputs.SecurityGroupEgressArgs;

import java.util.Map;

public class App {

    public static void main(String[] args) {

        Pulumi.run(ctx -> {

            // VPC
            var vpc = new Vpc(
                "web-server-fleet-vpc",
                VpcArgs.builder()
                    .cidrBlock("10.0.0.0/16")
                    .enableDnsHostnames(true)
                    .enableDnsSupport(true)
                    .tags(Map.of(
                        "Name", "web-server-fleet-vpc"
                    ))
                    .build()
            );

            // Subnet A
            var subnetA = new Subnet(
                "web-server-subnet-a",
                SubnetArgs.builder()
                    .vpcId(vpc.id())
                    .cidrBlock("10.0.1.0/24")
                    .availabilityZone("us-east-1a")
                    .mapPublicIpOnLaunch(true)
                    .tags(Map.of(
                        "Name", "web-server-subnet-a"
                    ))
                    .build()
            );

            // Subnet B
            var subnetB = new Subnet(
                "web-server-subnet-b",
                SubnetArgs.builder()
                    .vpcId(vpc.id())
                    .cidrBlock("10.0.2.0/24")
                    .availabilityZone("us-east-1b")
                    .mapPublicIpOnLaunch(true)
                    .tags(Map.of(
                        "Name", "web-server-subnet-b"
                    ))
                    .build()
            );

            // Internet Gateway
            var internetGateway = new InternetGateway(
                "web-server-fleet-igw",
                InternetGatewayArgs.builder()
                    .vpcId(vpc.id())
                    .tags(Map.of(
                        "Name", "web-server-fleet-igw"
                    ))
                    .build()
            );

            // Public Route Table
            var routeTable = new RouteTable(
                "web-server-fleet-public-rt",
                RouteTableArgs.builder()
                    .vpcId(vpc.id())
                    .tags(Map.of(
                        "Name", "web-server-fleet-public-rt"
                    ))
                    .build()
            );

            // Internet Route
            var route = new Route(
                "web-server-fleet-public-route",
                RouteArgs.builder()
                    .routeTableId(routeTable.id())
                    .destinationCidrBlock("0.0.0.0/0")
                    .gatewayId(internetGateway.id())
                    .build()
            );

            // Subnet A → Route Table
            var routeTableAssociationA = new RouteTableAssociation(
                "web-server-fleet-subnet-a-rt-association",
                RouteTableAssociationArgs.builder()
                    .routeTableId(routeTable.id())
                    .subnetId(subnetA.id())
                    .build()
            );

            // Subnet B → Route Table
            var routeTableAssociationB = new RouteTableAssociation(
                "web-server-fleet-subnet-b-rt-association",
                RouteTableAssociationArgs.builder()
                    .routeTableId(routeTable.id())
                    .subnetId(subnetB.id())
                    .build()
            );

            // Security Group
            var webSecurityGroup = new SecurityGroup(
                "web-server-fleet-sg",
                SecurityGroupArgs.builder()
                    .vpcId(vpc.id())
                    .ingress(
                        SecurityGroupIngressArgs.builder()
                            .protocol("tcp")
                            .fromPort(80)
                            .toPort(80)
                            .cidrBlocks("0.0.0.0/0")
                            .build()
                    )
                    .egress(
                        SecurityGroupEgressArgs.builder()
                            .protocol("-1")
                            .fromPort(0)
                            .toPort(0)
                            .cidrBlocks("0.0.0.0/0")
                            .build()
                    )
                    .tags(Map.of("Name", "web-server-fleet-sg"))
                    .build()
            );

            var fleet = new WebServerFleet(
                "customer-web-fleet",
                new WebServerFleetArgs(
                    java.util.List.of(
                        subnetA.id(),
                        subnetB.id()
                    ),
                    java.util.List.of(
                        new MachineConfig(
                            OperatingSystem.UBUNTU,
                            MachineSize.SMALL,
                            1
                        ),
                        new MachineConfig(
                            OperatingSystem.AMAZON_LINUX,
                            MachineSize.MEDIUM,
                            1
                        )
                    ),
                    webSecurityGroup.id()
                )
            );

            // Pulumi stack outputs
            ctx.export("vpcId", vpc.id());
            ctx.export("subnetAId", subnetA.id());
            ctx.export("subnetBId", subnetB.id());
            ctx.export("internetGatewayId", internetGateway.id());
            ctx.export("routeTableId", routeTable.id());
            ctx.export("routeId", route.id());
            ctx.export("routeTableAssociationAId", routeTableAssociationA.id());
            ctx.export("routeTableAssociationBId", routeTableAssociationB.id());
            ctx.export("webSecurityGroupId", webSecurityGroup.id());
            ctx.export(
                    "instanceIds",
                    Output.all(
                        fleet.getInstances().stream()
                            .map(Instance::id)
                            .toList()
                    )
                );

                ctx.export(
                    "publicIps",
                    Output.all(
                        fleet.getInstances().stream()
                            .map(Instance::publicIp)
                            .toList()
                    )
                );
                        });
    }
}