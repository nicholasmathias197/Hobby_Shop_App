#!/bin/bash
systemctl stop mariadb
sleep 2
killall mysqld_safe mysqld mariadbd 2>/dev/null || true
sleep 3
mysqld_safe --skip-grant-tables --skip-networking &
sleep 6
mysql -u root <<'SQL'
FLUSH PRIVILEGES;
ALTER USER 'root'@'localhost' IDENTIFIED VIA mysql_native_password USING PASSWORD('Postgres1');
FLUSH PRIVILEGES;
SQL
killall mysqld_safe mysqld mariadbd 2>/dev/null || true
sleep 3
systemctl start mariadb
sleep 5
systemctl restart hobby-shop
echo "Done"
