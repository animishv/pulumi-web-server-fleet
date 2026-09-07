# Pulumi Web Server Fleet

A Java-based Pulumi `ComponentResource` that provides a reusable abstraction for provisioning a fleet of AWS EC2 web servers across multiple operating systems and machine sizes.

## What This Project Demonstrates

- Pulumi `ComponentResource` abstraction
- AWS VPC and subnet networking
- EC2 instance provisioning
- Ubuntu and Amazon Linux support
- Abstract machine sizes mapped to AWS instance types
- OS-specific AMI selection
- nginx installation through EC2 UserData
- Distribution of instances across multiple subnets
- Pulumi Outputs and resource dependencies

## Architecture

```text
                         Internet
                            |
                    Internet Gateway
                            |
                 +----------+----------+
                 |       AWS VPC       |
                 |                     |
          Public Subnet A       Public Subnet B
                 |                     |
              EC2 Fleet             EC2 Fleet
                 |                     |
          Ubuntu / Amazon Linux instances
                            |
                           nginx
```

The developer-facing abstraction does not require knowledge of AWS AMI IDs or EC2 instance types. It accepts an operating system and abstract machine size, and the component maps those values to AWS-specific resources.

## Example Usage

```java
var fleet = new WebServerFleet(
    "customer-web-fleet",
    new WebServerFleetArgs(
        List.of(subnetA.id(), subnetB.id()),
        List.of(
            new MachineConfig(
                OperatingSystem.UBUNTU,
                MachineSize.SMALL,
                3
            ),
            new MachineConfig(
                OperatingSystem.AMAZON_LINUX,
                MachineSize.MEDIUM,
                2
            )
        ),
        webSecurityGroup.id()
    )
);
```

This example provisions five EC2 instances:

- 3 Ubuntu small instances
- 2 Amazon Linux medium instances

## Abstraction

### Operating Systems

```text
UBUNTU       -> Ubuntu 24.04 LTS AMI
AMAZON_LINUX -> Amazon Linux 2023 AMI
```

### Machine Sizes

```text
SMALL  -> t3.micro
MEDIUM -> t3.small
LARGE  -> t3.medium
```

The mappings are centralized in `AwsMachineMapper`, keeping AWS-specific implementation details out of the fleet interface.

## Project Structure

```text
pulumi-web-server-fleet/
├── Pulumi.yaml
├── Pulumi.dev.yaml
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        └── java/
            └── com/
                └── pulumi/
                    └── webserverfleet/
                        ├── App.java
                        ├── AwsMachineMapper.java
                        ├── MachineConfig.java
                        ├── MachineSize.java
                        ├── OperatingSystem.java
                        ├── UserDataBuilder.java
                        ├── WebServerFleet.java
                        └── WebServerFleetArgs.java
```

## Prerequisites

- Java 21+
- Apache Maven
- Pulumi CLI
- AWS CLI with valid AWS credentials
- AWS account

## Execute the Project

Clone the repository:

```bash
git clone https://github.com/animishv/pulumi-web-server-fleet.git
cd pulumi-web-server-fleet
```

Authenticate with AWS using your normal AWS CLI authentication method:

```bash
aws login
```

Verify that the CLI can access your AWS account:

```bash
aws sts get-caller-identity
```

Build the Java project:

```bash
mvn clean package
```

Select the Pulumi stack:

```bash
pulumi stack select dev
```

The project is configured for `us-east-1` in `Pulumi.dev.yaml`.

Preview the infrastructure:

```bash
pulumi preview
```

Deploy the infrastructure:

```bash
pulumi up
```

Review the proposed changes and confirm with `yes`.

After deployment, Pulumi exports:

- EC2 instance IDs
- Public IP addresses
- VPC ID
- Subnet IDs
- Internet Gateway ID
- Route table information
- Security group ID

View the stack outputs at any time with:

```bash
pulumi stack output
```

## Test the Created AWS Resources

The example creates two EC2 instances for testing:

- One Ubuntu `SMALL` instance
- One Amazon Linux `MEDIUM` instance

The instances receive public IP addresses and nginx is installed automatically through EC2 UserData.

### 1. Get the public IP addresses

```bash
pulumi stack output publicIps
```

### 2. Test HTTP connectivity

For each returned public IP:

```bash
curl -I http://<public-ip>
```

A successful response should contain:

```text
HTTP/1.1 200 OK
Server: nginx
```

### 3. Verify the application page

```bash
curl http://<public-ip>
```

The Ubuntu instance should return content similar to:

```html
<h1>Hello from Ubuntu!</h1>
<p>Provisioned by Pulumi WebServerFleet.</p>
```

The Amazon Linux instance returns an equivalent page identifying Amazon Linux.

### 4. Test from a browser

Open:

```text
http://<public-ip>
```

The nginx-generated page should be displayed.

### 5. Verify resources in AWS

The deployment can also be inspected through the AWS Console. The stack creates:

- 1 VPC
- 2 public subnets
- 1 Internet Gateway
- 1 route table
- 2 route table associations
- 1 security group
- EC2 instances defined by the fleet configuration

## Notes for Interviewer

### Ubuntu AWS
<img width="1722" height="872" alt="UbuntuAWS" src="https://github.com/user-attachments/assets/b9d54eed-c4e6-4323-8e2d-ee05eaa1a112" />
### Ubuntu Instance Running
<img width="666" height="142" alt="Ubuntu Instance" src="https://github.com/user-attachments/assets/a648b0f4-4707-4a7f-ab2d-027fe9413cff" />
### pulumi preview
<img width="942" height="539" alt="pulumi_preview" src="https://github.com/user-attachments/assets/454b915a-0de2-41d3-af3f-b871fdd3f329" />
### pulumi up
<img width="890" height="886" alt="Pulumi up" src="https://github.com/user-attachments/assets/14640eb8-aa09-46b8-b0fa-359f6ef6b424" />
### Pulumi Console
<img width="1717" height="813" alt="Pulumi Console" src="https://github.com/user-attachments/assets/85634997-ab96-4c57-a32e-2346eed30629" />
### AWS EC2 Console
<img width="662" height="135" alt="EC2 Instance" src="https://github.com/user-attachments/assets/1f08c379-ee6a-4228-adc6-f342ccf8d94e" />
### Amazon Medium AWS
<img width="1724" height="871" alt="AmazonMediumAWS" src="https://github.com/user-attachments/assets/87772295-8a3a-488a-a0da-d17cd07e7e80" />


## Cleanup

The test resources incur AWS charges while they exist. Destroy them when testing is complete:

```bash
pulumi destroy
```

Confirm with `yes`.

Verify that the stack no longer has deployed resources:

```bash
pulumi stack
```

## Design Notes

### Why a ComponentResource?

`WebServerFleet` groups multiple underlying AWS resources behind a single reusable abstraction. Consumers specify what they want rather than how each EC2 instance should be created.

### Why abstract OS and machine size?

The application-facing configuration should not need to know AWS-specific AMI IDs or EC2 instance type names. Those implementation details are isolated in `AwsMachineMapper`.

### Networking

The example creates a minimal AWS VPC with two public subnets, an Internet Gateway, route table, and security group. EC2 instances are distributed across the supplied subnets.

### State

Pulumi manages the infrastructure state for the `dev` stack. The local `.pulumi/` directory is excluded from Git, and the stack's non-secret AWS region configuration is stored in `Pulumi.dev.yaml`.

## Technologies

- Java
- Maven
- Pulumi
- Pulumi AWS Provider
- Amazon VPC
- Amazon EC2
- nginx
