#!/bin/bash
set -e

INSTANCE_ID=$(aws ec2 describe-instances \
  --filters "Name=tag:Name,Values=gundam-hobby-shop-backend" "Name=instance-state-name,Values=running" \
  --query "Reservations[0].Instances[0].InstanceId" \
  --output text --region us-east-2)

echo "Target instance: $INSTANCE_ID"

for i in $(seq 1 20); do
  STATUS=$(aws ssm describe-instance-information \
    --filters "Key=InstanceIds,Values=$INSTANCE_ID" \
    --query "InstanceInformationList[0].PingStatus" \
    --output text --region us-east-2 2>/dev/null)
  echo "SSM status $i/20: $STATUS"
  [ "$STATUS" = "Online" ] && break
  sleep 15
done

COMMAND_ID=$(aws ssm send-command \
  --instance-ids "$INSTANCE_ID" \
  --document-name "AWS-RunShellScript" \
  --parameters 'commands=["aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobby-shop-backend.jar /opt/hobby-shop/hobby-shop-backend.jar","systemctl restart hobby-shop"]' \
  --region us-east-2 \
  --query "Command.CommandId" --output text)

echo "SSM command ID: $COMMAND_ID"
aws ssm wait command-executed --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" --region us-east-2
echo "Spring Boot restarted successfully"
