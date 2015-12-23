项目介绍:

本项目是基于 Maven + Git 进行构建和管理的, 主要用于抓取 2015 B萌 本战阶段相关数据.

下载安装:

```
1. git clone https://github.com/fuhaiwei/bilibili-moe-spider.git
2. cd bilibili-moe-spdier
3. mvn clean compile
```

自动运行统计:

```
1. mvn test
```

结果输出在:

```
1. output/01 当前比赛结果.txt
2. output/benzhan/<date>/01 本战比赛情况 <time>.txt
3. output/benzhan/<date>/02 本战票差情况 <time>.txt
```

运行特定统计:

```
1. 手动抓取并统计当前数据
mvn exec:java -Dexec.mainClass="intopass.bmoe.MainCount"

2. 统计特定角色历史数据
mvn exec:java -Dexec.mainClass="intopass.bmoe.TempCount"

3. 统计弃票率数据
mvn exec:java -Dexec.mainClass="intopass.bmoe.LostCount"
```

更新以往数据:

```
1. git fetch origin output
2. git checkout output -- output
3. git reset
```
