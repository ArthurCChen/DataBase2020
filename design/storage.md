![image-20200501213104985](C:\Users\DELL\AppData\Roaming\Typora\typora-user-images\image-20200501213104985.png)



 ![img](https://www.sqlite.org/images/arch2.gif) 

 https://blog.csdn.net/pfysw/category_7222550.html 

 ![clip_image002[4]](https://img-blog.csdn.net/20180121182529294) 

 https://blog.csdn.net/qq_37940313/category_7667448.html 

暂时实现的是

单线程、主索引（无二级索引—）、无页式缓存

# Storage

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