1. 参考项目
   1.  http://courses.cms.caltech.edu/cs122/assignments/ 
2. 我要做的事（5/28 通宵）
   1. ColumnType
   2. Column Row ColumnDesc 仔细思考对应关系并实现
   3. 先写heapfilehandler，给一个弄够直接用的接口
      1. filehandler相关
      2. 序列化相关
   4. 跑通Table和Database
   5. 写btreefilehandler，实现btree文件索引

底层接口:

1. 内存上跑/缓存
2. 有transaction的,事务开始事务结束
3. 回写
4. OE遍历，快一些
5. 添加表删除表
6. 增删tuple

