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
