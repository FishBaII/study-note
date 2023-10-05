
## introduction
[KQL官方文档](https://www.elastic.co/guide/en/kibana/8.10/kuery-query.html) 

Kibana 查询语言 （KQL） 是一种用于筛选数据的简单基于文本的查询语言。

* KQL 仅**筛选数据**，在聚合、转换或排序数据方面没有任何作用。
* 不要将 KQL 与具有不同功能集的 Lucene 查询语言混淆。
使用 KQL 筛选字段值存在、与给定值匹配或位于给定范围内的文档。


## example

### 字段匹配
```
response:200
```
　　上面这个表达式，会查询出response字段中包含200的文档对象，注意是包含，包含的是200这一个词，比如下面几种情况都会被查询出来
```
200
hello world 200
hello 200 world
```
需要注意的是1200或者2001，是不能被查出来的。

如果要查询1200或者2001，这种模糊匹配的，可以使用通配符，比如response:*200或者response:200*

### 全匹配

```
Hello
```

匹配所有字段中出现**hello**（大小写忽略）的文档

### 文本查询
```
message:"hello world yes"
```

上面这个表达式，是针对message字段进行搜索，在搜索的时候不会区分大小写，也就是说，Hello world YES也是会被搜索出来的；

需要注意，上面的"hello world yes"使用了引号，这样的话，这3个单词会被作为一个词进行查询，不会再进行分词，也就是说匹配的时候只会匹配hello world yes这样的顺序匹配，而不会匹配出helllo yes world；



### 组合查询or
```
message:hello world
message:(hello or world)
message:hello or message:world
```
　　上面这个表达式，针对message字段进行搜索，搜索message中包含hello，或者包含world，或者两者都包含的情况；

需要注意的是，不区分大小写，也不会保证顺序，也就是说，下面几种情况都会被匹配

```
hello
world
Hello
World
hello world
Hello world
hello yes World
yes world
world yes

```





### 组合查询and

```
name:jane and addr:beijing or job:teacher
(name:jane and addr:beijing) or job:teacher
```

上面这个查询条件中，出现了and和or，需要记住的是，KQL中，and的优先级高于or；

所以上面的查询条件，会查询name包含jane，且addr包含beijing的记录，或者job包含teacher的记录，可以使用括号来让上面的查询条件更好理解：



### 优先级指定

```
name:jane and (addr:beijing or job:teacher)
```

可以使用括号来控制匹配的优先级。



### 否定查询

```
not response:200
```

上面这个查询条件，会查询出response字段中不包含200的记录。



```
response:200 and not yes
```

上面这个查询条件，会查询response包含200，并且整条记录不包含yes的数据记录；



```
response:(200 and not yes)
```

上面这个查询条件，会查询response包含200，且response不包含yes的记录。



### 通配符查询

```
response:*
```

上面这个查询条件，会返回所有包含response字段的文档对象。

```
machine*:hello
```

上面这个查询条件，会查询machine1字段，machine2字段...machinexyzabc字段包含hello的数据记录，这里只是想表达，对于搜索的字段列，也是可以使用通配符的。

### 时间范围

根据时间戳搜索两周前的文档
```
@timestamp < now-2w
```

### 嵌套查询

示例文档
```
{
  "user": [
    {
      "names": [
        {
          "first": "John",
          "last": "Smith"
        },
        {
          "first": "Alice",
          "last": "White"
        }
      ]
    }
  ]
}
```

若要查找数组中单个值包含名字“Alice”和姓氏“White”的文档，请使用以下命令

```
user.names:{ first: "Alice" and last: "White" }
```


### 转义字符

```
http.request.referrer: "https://example.com"
http.request.referrer: https\://example.com
```
以上KQL是等效的，以下字符需要转义字符

```
\():<>"*
```

