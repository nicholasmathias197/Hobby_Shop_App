#!/bin/bash
mysql -u root -pPostgres1 -e "CREATE DATABASE IF NOT EXISTS hobby_shop_db;"
aws s3 cp s3://gundam-hobby-shop-frontend-911784620581/app/hobbyshop.sql /tmp/hobbyshop.sql
mysql -u root -pPostgres1 hobby_shop_db < /tmp/hobbyshop.sql
echo "Schema loaded"
systemctl restart hobby-shop
echo "Done"
