# distributed
## 金丝雀发布（灰度发布）
### 方案:使用节点分支管理方式
 - ：nacos(管理节点版本)+openfeign(远程调用)+loadbalance(选取节点)
- ![cannary.png](..%2Fdoc%2Fcannary%2Fcannary.png)
- 核心负载均衡代码：GreyLoadBalancer
- 原理解释博客地址:https://mp.weixin.qq.com/s/-erjoXmgSxHh51ZZjcAOHQ
