<a name="EVM4v"></a>
## 简介

Stream流是JDK8新增的成员，允许以声明性方式处理数据集合，可以把Stream流看作是遍历数据集合的一个高级迭代器。<br />Stream 是 Java8 中处理集合的关键抽象概念，它可以指定你希望对集合进行的操作，可以执行非常复杂的查找/筛选/过滤、排序、聚合和映射数据等操作。<br />使用Stream API 对集合数据进行操作，就类似于使用 SQL 执行的数据库查询。也可以使用 Stream API 来并行执行操作。

> 数据源：流的数据来源，构造Stream对象的数据源，比如通过一个List来构造Stream对象，这个List就是数据源
> 聚合操作：对Stream对象进行处理后使得Stream对象返回指定规则数据的操作称之为聚合操作，比如filter、map、limit、sorted等都是聚合操作


<a name="ixPhd"></a>
## 常用API
举例实体类Student和测试List如下：
```java
public class Student {
    
    private String name;
    private String age;
    private String code;
    
}
```
```java
List<Student> list = new ArrayList<>();
list.add(new Student("tom", "1", "101"));
list.add(new Student("jerry", "1", "101"));
list.add(new Student("apple", "2", "208"));
list.add(new Student("banana", "2", "209"));
```
<a name="QRup2"></a>
### 
<a name="PNLDm"></a>
### collect 转换结构
```java
        // 将list转换为name为key的Map
        Map<String, Student> nameMap = list.stream()
                .collect(Collectors.toMap(s -> s.getName(), s -> s));

//{banana=Student{name='banana', age=2, code=209}, 
//apple=Student{name='apple', age=2, code=208}, 
//tom=Student{name='tom', age=1, code=101}, 
//jerry=Student{name='jerry', age=1, code=101}}
```
<a name="nziJA"></a>
### [filter](http://www.macrozheng.com/#/technology/java_stream?id=filter) 过滤
```java
// 获取年龄为1的Student() 
List ageList = list.stream()
        .filter(s -> Objects.equals(s.getAge(), "1"))
        .collect(Collectors.toList());
//{"tom", "1", "101"}，{"jerry", "1", "101"}
```
<a name="Fza70"></a>
### groupBy 对元素进行分组
```java
//按age进行分组        
Map<String, List<Student>> ageMap = list.stream()
	//.collect(Collectors.groupingBy(s->s.getAge()));
	.collect(Collectors.groupingBy(Student::getAge));
//{1=[Student{name='tom', age=1, code=101}, Student{name='jerry', age=1, code=101}], 
//2=[Student{name='apple', age=2, code=208}, Student{name='banana', age=2, code=209}]}

```

<a name="dOgH8"></a>
### map 对Stream中的元素进行转换处理后获取
```java
// 获取所有name组成的集合
List nameList = list.stream()
    .map(s -> s.getName())
    .collect(Collectors.toList());
//tom,jerry,apple,banana
```

<a name="count"></a>
### [count](http://www.macrozheng.com/#/technology/java_stream?id=count) 仅获取Stream中元素的个数。
```java
// count操作：获取code为null的数目
long count = list.stream()
	.filter(s -> s.getCode() == null)
	.count();
// count = 0
```


<a name="sorted"></a>
### [sorted](http://www.macrozheng.com/#/technology/java_stream?id=sorted) 对Stream中元素按指定规则进行排序。
```java
// 将list元素按age从小到大进行排序
List<Student> sortedList = list.stream()
	.sorted(Comparator.comparing(Student::getAge))
	.collect(Collectors.toList());

List<Student> sortedList = list.stream()
	.sorted((student1, student2) -> {
		return student1.getAge().compareTo(student2.getAge());
	})
	.collect(Collectors.toList());

//[Student{name='tom', age=1, code=101}, Student{name='jerry', age=1, code=101}, 
//Student{name='apple', age=2, code=208}, Student{name='banana', age=2, code=209}]
```

<a name="ETWLu"></a>
### max和min 求最大和最小值
```java
Optional<Student> max = list.stream()
	.max(Comparator.comparing(stu -> stu.getCode()));
Optional<Student> min = list.stream()
	.min(Comparator.comparing(stu -> stu.getCode()));
//判断是否有值
if (max.isPresent()) {
	System.out.println(max.get());
	//209
}
if (min.isPresent()) {
	System.out.println(min.get());
	//101
        }
```

<a name="bJT89"></a>
##  综合举例
举例实体类和List数据如下
```java
public class Student {

    private String name;
    private String age;
    private String code;
}

List<Student> list = new ArrayList<>();
list.add(new Student("tom", "1", "101"));
list.add(new Student("jerry", "1", "101"));
list.add(new Student("apple", "2", "208"));
list.add(new Student("banana", "2", "209"));
```
需求如下：以age和code作为一个分组条件，合并显示相同age和code的student的name（以逗号分隔）<br />期望结果：[{"tom,jerry" , 1, 101}, {"apple", 2, 208}, {"banana", 2, 209}]<br />效率对比：方法2略快

<a name="rrGZQ"></a>
### 方法1
```java
        //利用groupby将age和code进行字符串拼接作为一个分组条件
		//组合为一个age_code为key,name以逗号进行分隔归组为value的map
		Long startTime1 = System.currentTimeMillis();
        Map<String, String> map2 = list1.stream().collect(Collectors.groupingBy(o->o.getAge()+"_"+o.getCode(),
                Collectors.mapping(Student::getName, Collectors.joining(","))));



        //map2: {2_208=apple, 2_209=banana, 1_101=tom,jerry}

		//
        List<Student> newList = list1.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(o -> o.getCode() + ";" + o.getAge()))), ArrayList::new));


        for(Student student: newList){
            student.setName(map2.get(student.getAge() + "_" + student.getCode()));
        }
        Long endTime1 = System.currentTimeMillis();
        System.out.println(newList.size() + " one:"+(endTime1-startTime1));
        System.out.println(newList.toString());
```

<a name="Ex89J"></a>
### 方法2
```java
		//依次对list进行两次分组(code和age)
        Long startTime2=System.currentTimeMillis();
        Map<String,Map<String, List<Student>>> result = list2.stream().collect(
                Collectors.groupingBy(Student::getCode,
                        Collectors.groupingBy(Student::getAge
                                ))

		);

		//result: {101={1=[Student{name='tom', age=1, code=101}, Student{name='jerry', age=1, code=101}]}, 
		//208={2=[Student{name='apple', age=2, code=208}]}, 
		//209={2=[Student{name='banana', age=2, code=209}]}}

        List<Student> finallyResult = new ArrayList<>();
		//用迭代器分别进行遍历result和result.value，然后创建目标list
        Iterator<Map.Entry<String,Map<String, List<Student>>>> firstIterator = result.entrySet().iterator();
        while (firstIterator.hasNext()){
            Map.Entry<String,Map<String, List<Student>>> entry = firstIterator.next();
            //String code = entry.getKey();
            Map<String, List<Student>> ageMap = entry.getValue();
            Iterator<Map.Entry<String, List<Student>>> ageIterator = ageMap.entrySet().iterator();

            while (ageIterator.hasNext()){
                Map.Entry<String, List<Student>> entry1 = ageIterator.next();
                //List<Student> ageList = entry1.getValue();
                String nameStr = entry1.getValue().stream().collect(Collectors.mapping(Student::getName, Collectors.joining(",")));
                finallyResult.add(new Student(nameStr, entry1.getKey(), entry.getKey()));
            }

        }


        Long endTime2 = System.currentTimeMillis();
        System.out.println(finallyResult.size() + " two:"+(endTime2-startTime2));
        System.out.println(finallyResult.toString());
```
