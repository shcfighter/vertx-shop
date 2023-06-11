#!/bin/sh

# 杀shop微服务进程
ps -aux | grep shop- | grep -v grep | awk '{print $2}' | while read pid
do
	echo "shop- is running, to kill bootstrap pid=$pid"
	kill -9 $pid
	echo "kill result: $?"
done

# 删除log
echo "remove logs"
rm -rf /data/logs/shop-*.log

# mvn clean install

#启动服务
nohup java -Xms512m -Xmx512m -jar /data/vertx-shop/shop-gateway/target/shop-gateway-1.0-SNAPSHOT-fat.jar -conf /data/vertx-shop/shop-gateway/src/main/resources/gateway-config.json -cluster >> /data/logs/shop-gateway.log &
echo 'Gateway Micro Services start ...'
sleep 10s
nohup java -Xms512m -Xmx512m -jar /data/vertx-shop/shop-user-module/shop-user/target/shop-user-1.0-SNAPSHOT-fat.jar -conf /data/vertx-shop/shop-user-module/shop-user/src/main/resources/user-config.json -cluster >> /data/logs/shop-user.log &
echo 'User Micro Services start ...'
sleep 10s
nohup java -Xms512m -Xmx512m -jar /data/vertx-shop/shop-search-module/shop-search/target/shop-search-1.0-SNAPSHOT-fat.jar -conf /data/vertx-shop/shop-search-module/shop-search/src/main/resources/search-config.json -cluster >> /data/logs/shop-search.log &
echo 'Search Micro Services start ...'
sleep 20s
nohup java -Xms512m -Xmx512m -jar /data/vertx-shop/shop-message-module/shop-message/target/shop-message-1.0-SNAPSHOT-fat.jar -conf /data/vertx-shop/shop-message-module/shop-message/src/main/resources/message-config.json -cluster >> /data/logs/shop-message.log &
echo 'Message Micro Services start ...'
sleep 10s
nohup java -Xms512m -Xmx512m -jar /data/vertx-shop/shop-order-module/shop-order/target/shop-order-1.0-SNAPSHOT-fat.jar -conf /data/vertx-shop/shop-order-module/shop-order/src/main/resources/order-config.json -cluster >> /data/logs/shop-order.log &
echo 'Order Micro Services start ...'

