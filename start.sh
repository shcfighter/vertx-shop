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
rm -rf shop-*.log

#启动服务
nohup java -Xms64m -Xmx64m -jar shop-gateway-1.0-SNAPSHOT-fat.jar -conf gateway-config.json -cluster >> shop-gateway.log &
echo 'Gateway Micro Services start ...'
sleep 10s
nohup java -Xms64m -Xmx64m -jar shop-user-1.0-SNAPSHOT-fat.jar -conf user-config.json -cluster >> shop-user.log &
echo 'User Micro Services start ...'
sleep 10s
nohup java -Xms64m -Xmx64m -jar shop-search-1.0-SNAPSHOT-fat.jar -conf search-config.json -cluster >> shop-search.log &
echo 'Search Micro Services start ...'
sleep 20s
nohup java -Xms64m -Xmx64m -jar shop-message-1.0-SNAPSHOT-fat.jar -conf message-config.json -cluster >> shop-message.log &
echo 'Message Micro Services start ...'
sleep 10s
nohup java -Xms64m -Xmx64m -jar shop-order-1.0-SNAPSHOT-fat.jar -conf order-config.json -cluster >> shop-order.log &
echo 'Order Micro Services start ...'

