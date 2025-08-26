# generator
​	根据后端表结构，生成Java后端SpringBoot/go后端和前端Vue组件相关代码

***

​	PS：项目原始代码来源于[若依框架](https://gitee.com/y_project/RuoYi-Vue.git)，本人对[若依框架](https://gitee.com/y_project/RuoYi-Vue.git)代码生成部分进行单独提取并且进一步对代码生成模板进行更佳通用的适配修改，以至于不局限于[若依框架](https://gitee.com/y_project/RuoYi-Vue.git)内部使用。

***

## 项目拉取

```
git clone https://github.com/nhjclxc/generator.git
```



## 运行项目

### 运行后端

1. 进入后端项目

   ​	`cd generator`

2. 打包后端

   ​	`mvn clean package`

3. 启动

   ​	`java -jar generator-0.0.1-SNAPSHOT.jar` 
   ​	`nohup java -jar generator-0.0.1-SNAPSHOT.jar > nohup.log 2>&1 &` 

后端项目启动在9099端口



### 运行前端

1. 进入前端项目

   ​	`cd generator-ui`

2. 打包前端

   ​	`npm install`

3. 启动

   ​	`npm run serve` 

前端项目启动在9098端口


### docker启动一个mysql
// 镜像网站：https://docker.aityp.com/image/docker.io/mysql:8.0

docker pull swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/mysql:8.0
docker tag  swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/mysql:8.0  docker.io/mysql:8.0

mkdir -p /home/mysql/data /home/mysql/conf  /home/mysql/logs

sudo chown -R 999:999 /home/mysql/data /home/mysql/conf  /home/mysql/logs

```shell
docker run -d \
--name mysql80 \
-e MYSQL_ROOT_PASSWORD=root123 \
-p 3306:3306 \
-v /home/mysql/data:/var/lib/mysql \
-v /home/mysql/conf:/etc/mysql/conf.d \
-v /home/mysql/logs:/var/log/mysql \
--restart always \
mysql:8.0
```

docker logs -f mysql80

docker exec -it mysql80 bash

mysql -u root -p

## 使用案例

![1712304509162](case.png)
