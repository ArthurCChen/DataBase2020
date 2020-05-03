#  存储模块设计文档 

 

 https://blog.csdn.net/pfysw/category_7222550.html 

 ![clip_image002[4]](https://img-blog.csdn.net/20180121182529294) 

 https://blog.csdn.net/qq_37940313/category_7667448.html 

暂时实现的是

单线程、主索引（无二级索引—）、无页式缓存

## 架构说明

本次我们的数据库存储模块参考了[Sqlite的存储模块架构]( https://www.sqlite.org/arch.html )（即：左下部分）。具体来说，存储模块自上向下分为：（括号内为其在代码中对应的模块）

1. B-Tree File Handler（`FileHandler`）
   1. 负责提供接口给虚拟机部分，以使其可以和硬盘进行操作
2. Page Cache(`BufferPool`)
   1. 负责缓存最近使用的Cache，在`File Handler`和硬盘之间，负责方便文件内容读取。
   2. 使用`Clock`置换算法
3. Page(`Page`)
   1. 是`File Handler`和`Cache`中读取硬盘的基本单位



 ![img](https://www.sqlite.org/images/arch2.gif)

下面以本次实验的要求，利用伪码来说明我们架构的逻辑顺序

## 序列化

序列化发生在：`Page Cache`在缓存溢出或者用户保存时，将脏标记为`true`了的`Page`调用`FileHandler.writePage(page)`写回文件。

反序列化发生在：`Page Cache`在读取未缓存的页时，调用`FileHandler.readPage(page)`对文件中某一区间内的二进制文件进行反序列化。

## 增删改查

在`FileHandler`中，提供了如下接口以便于上层虚拟机进行增删改查操作

```java
//增删改时返回
ArrayList<Page> insertRow(Row row);
ArrayList<Page> deleteRow(Row row);
ArrayList<Page> updateRow(Row row);
FileIterator iterator();
public interface FileIterator {
    public boolean hasNext();
    public Row next();
}
```
可以看出，这里增删改查的基本单位都是`一行`

而查找时，需要虚拟机层利自行利用`FileIterator`进行遍历来操作，我们认为二级索引可以由虚拟机自行维护。

## 五种数据类型

我们的数据库使用定长记录存储方案，其中String类型需要用户指定最大长度，并在序列化时先存其当前长度，之后自动补零（ascii）。

## 附注

考虑到方便分工，我们组员分工时并不是对单独的模块进行任务拆解，而是将不同的模块分配给不同人实现。因此，现有的存储模块的具体代码仍不完善，敬请谅解！其实现将在下一步同步推进。

Storage

参考sqlite的结构，我们将页式缓存数据结构作为B+树的最小保存原子单位，来与操作系统进行交互

存储模块结构

File



1. 磁盘文件封装
   1. I(x)  插入
   2. R(x) 读取
   3. U(x) 更新
2. 分页管理
   1. DM <--> cache <--> os cache <--> disk
3. 日志管理

## Database

* {string-->table}
* {string-->view}



## delete



## insert

物理添加





## View

* 

## Table

存储名

"databaseName/tableName .data"

## TODO: 分页存储

在 上述文件中以每16k字节为一页

 https://bbs.csdn.net/topics/300083048 