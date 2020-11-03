# whip

玩具项目, 练习一下 kotlin 语言的写法, 顺便解决一个项目进度跟踪问题.

她是一个项目进度跟踪工具, 会自动统计项目实施过程中各个关键节点的完成时间, 帮助统计项目的时间
主要花在哪些节点上.

几乎没有测试, Not Production Ready

很多代码来自于 spinnaker cloud driver. 过了一年再去看这个项目, 还是很难看懂, 太麻烦了.


几个细节

[1] Execution 有若干 stage 组成, stage 之间是并行的关系, 他们之间也支持有依赖,
依赖使用 required 声明
[2] Stage 内部由 step 组成, step 之间是严格的串行关系


流程引擎的最终目的是为了实现:
终极目的是为了实现, 发布系统的声明式发展, 比如

```java
plan.createBatches: List<Batch>
    .seriaMap(batch -> createHost): List<Host>
    .paralMap(host -> createStep): List<Step>
    .seriaForEach(step -> executeStep(step)) : void
```

1. 怎么解决同步异步的问题呢?

比如 executeStep 时, 每个 step 都是异步执行的. 我怎么定义函数的输入和输出?
如果 CreateBatch 也是异步执行的, 该怎么统计这个算子呢?

2. 怎么实现流式发布呢? 

比如 一个 batch 内部的 host 仅需要完成 99% 即可, 不需要全部完成, 需要定义