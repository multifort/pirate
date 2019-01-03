# pirates
Pirate is a platform to manage application with Kubernetes cluster and containers.   

| 工程名 | 端口 | 描述 |
| ------ | ------ | ------ |
| pirates | N/A | 父工程 |
|pirates-gateway | 9090	| 服务网关 | 
|pirates-config | 9091 | 配置中心 |  
|pirates-message | N/A	| 消息服务 |
|pirates-security |	N/A | 安全服务 | 
|pirates-common | N/A | 公共基础包 | 
|pirates-resource |	9092 | 资源服务，对资源进行操作 | 
|pirates-container | 9093 | 容器服务，对容器进行操作 | 
|pirates-devops | 9094 | DevOps服务，CI/CD操作 | 
|pirates-project | 9095 | 项目服务，对项目数据进行操作 | 
|pirates-application | 9096 |	应用服务，对应用数据进行操作 | 
|pirates-log | 9097 | 日志服务，对日志数据进行操作 | 
|pirates-monitor | 9098 | 监控服务，提供监测数据检索 | 
|pirates-alarm | 9099 | 告警服务，提供告警数据检索和策略设定 | 
|pirates-operation | 9100 | 运营服务，提供数据报表，计量计费等功能 | 
|pirates-user	 | 9101 | 用户服务，对用户数据操作 | 
|pirates-platform | 9102 | 平台服务，对环境，集群，配置，镜像库等操作 | 