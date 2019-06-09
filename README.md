# vertx 网上商城
基于vertx搭建的java分布式系统
- 前后端分离
- 前端管理项目 https://github.com/shcfighter/vertx-shop/tree/master/shop-ui
- 后端分多个模块；shop-gateway、shop-user-module、shop-search-module、shop-order-module、shop-message-module、shop-pay

---

## 技术栈
- JDK11、Vert.x、postgresql、mongodb、elasticsearch、rabbitmq、redis
- postgresql 存储数据
- mongodb 存储购物车记录、收藏记录、历史浏览记录
- elasticsearch 用于搜索商品
- rabbitmq 消息中间件
- reids 缓存中间件