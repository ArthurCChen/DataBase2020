#  存储模块设计文档 



 ![img](C:\Users\DELL\Desktop\arch2.gif) 

 https://blog.csdn.net/pfysw/category_7222550.html 

 ![clip_image002[4]](C:\Users\DELL\Desktop\20180121182529294) 

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

#  存储模块设计文档 

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

## 异常处理

在存储模块，除助教提供的：

- `DuplicateKeyException()`（主键出现重复的异常）

- `KeyNotExistException()`（键值不存在的异常）

我们还定义了：

- `FileNotExistException()`  (`deserialize`时找不到数据文件的异常)
- `ColumnNumDiscrepancyException() `(某行数据数目与列规定不符的异常)
- `ColumnTypeDiscrepancyException()` (某行数据类型与列规定不符的异常)
- `ColumnRuleDiscrepancyException()` (某行数据条件与列规定不符的异常)

## 单元测试

存储模块的单元测试在`TableTest.java`中，采用了junit4、junit5的一些特性，通过一些测试数据的检查达到了测试数据库增删改的效果。

## 附注

考虑到方便分工，我们组员分工时并不是对单独的模块进行任务拆解，而是将不同的模块分配给不同人实现。因此，现有的存储模块的具体代码仍不完善，敬请谅解！其实现将在下一步同步推进。

# 元数据管理模块

### 一、模块功能

该模块实现了如下功能：

 1.对数据库的表中的创建，删除

 2.数据库的创建，删除，修改

 3.实现表和数据库元数据的持久化 

4.重启数据库时从持久化的元数据中恢复系统信息。

### 二、数据结构

存储meta信息的有

```
script:存储的是sql语句
meta：存储的是序列化后的database信息
```

实现该模块的主要类及其功能有： 

1. Database 类：记录一个数据库的元信息。数据类型包括数据库名称（string），存储 的所有表的名称-表（hashmap<string, table>），数据库元数据的存储路径。
2. manager 类：记录当前用户下所有数据库的信息。数据类型包括数据库名称和数据 库的键值对，元数据文件存储路径，读写锁，当前操作的数据库。 

### 三、功能实现细节

如下：

 1.对

 2.数

 3.实

4.重

### 四、模块测试

在 test 文件夹中编写了 ManagerTest.java 和 DatabaseTest.java，对以上功能进行了测试。全部通过。 