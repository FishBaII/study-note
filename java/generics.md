# 泛型 Generics

## 介绍

Java 泛型（generics）是 JDK 5 中引入的一个新特性, 泛型提供了**编译时类型安全检测机制**，该机制允许程序员在编译时检测到非法的类型，
允许调用者在调用某个类的功能时传入一个或多个类型来定义该类的属性、方法的参数以及返回值的类型，这大大的提高了代码的灵活性。
泛型的本质是参数化类型，也就是说所操作的数据类型被指定为一个参数。编译器会检测实参类型是否符合泛型要求，而在获取元素时无需再造型，
编译器会自动添加造型代码。其实泛型的原型是Object。

```
//编译报错，不能传入非String类型
ArrayList<String> list = new ArrayList<>();
list.add(123);
```

### 类型擦除
java为每个泛型类型创建唯一的字节码表示，并且将该泛型类型的实例都映射到这个唯一的字节码表示上。将多种泛型类形实例映射到唯一的字节码表示是通过类型擦除（type erasue）实现的。
属于语法糖的一种，也就是说，对于 Java 虚拟机来说，他根本不认识Map<String, String> map这样的语法。需要在编译阶段通过类型擦除的方式进行解语法糖。
类型擦除的主要过程如下：
1. 将所有的泛型参数用其最左边界（最顶级的父类型）类型替换。 
2. 移除所有的类型参数。

```
Map<String, String> map = new HashMap<String, String>();
map.put("name", "ljm");
```

解语法糖后

```
Map map = new HashMap();
map.put("name", "ljm");
```
虚拟机中没有泛型，只有普通类和普通方法，所有泛型类的类型参数在编译时都会被擦除，泛型类并没有自己独有的Class类对象。
比如并不存在List<String>.class或是List<Integer>.class，而只有List.class。

那既然泛型信息编译的时候被擦除了，如何保证我们在集合中只添加指定的数据类型的对象呢？
> 其实在创建一个泛型类的对象时， Java 编译器是先检查代码中传入 < T > 的数据类型，并记录下来，然后再对代码进行编译，编译的同时进行类型擦除；
> 如果需要对被擦除了泛型信息的对象进行操作，编译器会自动将对象进行类型转换。

***

## 泛型的使用场景

尖括号 <> 中的 泛型标识被称作是类型参数，用于指代任何数据类型。

泛型标识是任意设置的（如果你想可以设置为 Hello都行），Java 常见的泛型标识以及其代表含义如下：

* T ：代表一般的任何类。
* E ：代表 Element 元素的意思，或者 Exception 异常的意思。
* K ：代表 Key 的意思。
* V ：代表 Value 的意思，通常与 K 一起配合使用。
* S ：代表 Subtype 的意思，文章后面部分会讲解示意。

创建对象时，需指定参数类型代替标识符，如留空则默认为Object

### 泛型类

#### 基本定义

类型参数用于类的定义中，则该类被称为泛型类。通过泛型可以完成对一组类的操作对外开放相同的接口。最典型的就是各种容器类，如：List、Set、Map等。


#### 基本语法

```
class 类名称 <泛型标识> {
  private 泛型标识 /*（成员变量类型）*/ 变量名; 
  .....

  }
}

```

#### 简单示例

```
public class Box<T> {
   
  private T t;
 
  public void add(T t) {
    this.t = t;
  }
 
  public T get() {
    return t;
  }
 
  public static void main(String[] args) {
    Box<Integer> integerBox = new Box<Integer>();
    Box<String> stringBox = new Box<String>();
 
    integerBox.add(new Integer(10));
    stringBox.add(new String("String"));
 
    System.out.printf("整型值为 :%d\n\n", integerBox.get());
    System.out.printf("字符串为 :%s\n", stringBox.get());
  }
}
// 编译以上代码，运行结果如下所示：
// 整型值为 :10
// 字符串为 :String
```

#### 详细介绍

在泛型类中，类型参数定义的位置有三处，分别为
* 非静态的成员属性类型
* 非静态方法的形参类型（包括非静态成员方法和构造器）
* 非静态的成员方法的返回值类型

```java
public class Generic<T> { 
    // key 这个成员变量的数据类型为 T, T 的类型由外部传入  
    private T key;

	// 泛型构造方法形参 key 的类型也为 T，T 的类型由外部传入
    public Generic(T key) { 
        this.key = key;
    }
    
	// 泛型方法 getKey 的返回值类型为 T，T 的类型由外部指定
    public T getKey(){ 
        return key;
    }
}

```

泛型类中的类型参数的确定是在创建泛型类对象的时候（例如 ArrayList< Integer >）。

而静态变量和静态方法在类加载时已经初始化，直接使用类名调用；
在泛型类的类型参数未确定时，静态成员有可能被调用，因此泛型类的类型参数是不能在静态成员中使用的

```java
public class Test<T> {    
    public static T one;   // 编译错误    
    public static T show(T one){ // 编译错误    
        return null;    
    }    
}  

```

静态泛型方法中可以使用自身的方法签名中新定义的类型参数，而不能使用泛型类中定义的类型参数。
```java
public class Test2<T> {   
	// 泛型类定义的类型参数 T 不能在静态方法中使用  
    public static <E> E show(E one){ // 这是正确的，因为 E 是在静态方法签名中新定义的类型参数    
        return one;    
    }    
}  

```

泛型类不只接受一个类型参数，它还可以接受多个类型参数

```java
public class MultiType <E,T> {
	E value1;
	T value2;
	
	public E getValue1(){
		return value1;
	}
	
	public T getValue2(){
		return value2;
	}
}

```

### 泛型接口

#### 基本定义

与泛型类基本一致，泛型接口中的类型参数，在该接口被继承或者被实现时确定。

#### 基本语法

```
public interface 接口名<类型参数> {
    ...
}

```

#### 简单使用

```java
interface IUsb<U, R> {

    int n = 10;
    U name;// 报错！ 接口中的属性默认是静态的，因此不能使用类型参数声明

    R get(U u);// 普通方法中，可以使用类型参数

    void hi(R r);// 抽象方法中，可以使用类型参数

    // 在jdk8 中，可以在接口中使用默认方法, 默认方法可以使用泛型接口的类型参数
    default R method(U u) {
        return null;
    }
}

// 在继承泛型接口时，必须确定泛型接口的类型参数
interface IA extends IUsb<String, Double> {
	//...
}

// 当去实现 IA 接口时，因为 IA 在继承 IUsu 接口时，指定了类型参数 U 为 String，R 为 Double
// 所以在实现 IUsb 接口的方法时，使用 String 替换 U,用 Double 替换 R
class AA implements IA {
    @Override
    public Double get(String s) {
        return null;
    }
    @Override
    public void hi(Double d) {
		//...
    }
}
// 实现接口时，需要指定泛型接口的类型参数
// 给 U 指定 Integer， 给 R 指定了 Float
// 所以，当我们实现 IUsb 方法时，会使用 Integer 替换 U, 使用 Float 替换 R
class BB implements IUsb<Integer, Float> {
    @Override
    public Float get(Integer integer) {
        return null;
    }
    @Override
    public void hi(Float afloat) {
		//...
    }
}

// 实现泛型接口时没有确定类型参数，则默认为 Object
// 建议直接写成 IUsb<Object, Object>
class CC implements IUsb {//等价 class CC implements IUsb<Object, Object> 
    @Override
    public Object get(Object o) {
        return null;
    }
    @Override
    public void hi(Object o) {
    	//...
    }
}

// DD 类定义为 泛型类，则不需要确定 接口的类型参数
// 但 DD 类定义的类型参数要和接口中类型参数的一致
class DD<U, R> implements IUsb<U, R> { 
	//...
}

```


### 泛型方法

#### 基本定义

当在一个方法签名中的返回值前面声明了一个 < T > 时，该方法就被声明为一个泛型方法。< T >表明该方法声明了一个类型参数 T，并且这个类型参数 T 只能在该方法中使用。
当然，泛型方法中也可以使用泛型类中定义的泛型参数。

#### 基本语法

```
public <类型参数> 返回类型 方法名（类型参数 变量名） {
    ...
}
```

#### 基本用法

当调用泛型方法时，根据传入的实际对象，编译器会判断出类型形参 T 所代表的具体数据类型。
```
public class TestMethod {
	public <T, S> T testMethod(T t, S s) {
		return null;
	}
}

public class Demo {  
  public static void main(String args[]) {  
    TestMethod d = new TestMethod(); // 创建 GenericMethod 对象  
    d.testMethod("1", 1L);
  }  
}

```

#### 详细介绍

泛型类与泛型方法对比
```
public class Test<T> {
    
    //泛型类普通方法
	public void testMethod(T t) {
		System.out.println(t);
	}
	
	//泛型方法
	public <T> T testMethod1(T t) {
		return t;
	}
}

```

在调用泛型方法的时候，可以显式地指定类型参数，也可以不指定。

```
public class Test {

	// 这是一个简单的泛型方法  
    public static <T> T add(T x, T y) {  
        return y;  
    }

    public static void main(String[] args) {  
        // 一、不显式地指定类型参数
        //（1）传入的两个实参都是 Integer，所以泛型方法中的<T> == <Integer> 
        int i = Test.add(1, 2);
        
        //（2）传入的两个实参一个是 Integer，另一个是 Float，
        // 所以<T>取共同父类的最小级，<T> == <Number>
		Number f = Test.add(1, 1.2);

		// 传入的两个实参一个是 Integer，另一个是 String，
		// 所以<T>取共同父类的最小级，<T> == <Object>
        Object o = Test.add(1, "asd");
  
        // 二、显式地指定类型参数
        //（1）指定了<T> = <Integer>，所以传入的实参只能为 Integer 对象    
        int a = Test.<Integer>add(1, 2);
		
		//（2）指定了<T> = <Integer>，所以不能传入 Float 对象
        int b = Test.<Integer>add(1, 2.2);// 编译错误
        
        //（3）指定<T> = <Number>，所以可以传入 Number 对象
        // Integer 和 Float 都是 Number 的子类，因此可以传入两者的对象
        Number c = Test.<Number>add(1, 2.2); 
    }  
}

```

## 泛型通配符

### 介绍
在现实编码中，确实有这样的需求，希望泛型能够处理某一类型范围内的类型参数，比如某个泛型类和它的子类，为此 Java 引入了泛型通配符这个概念。

泛型通配符有 3 种形式：
* <?> ：被称作无限定的通配符。
* <? extends T> ：被称作有上界的通配符。
* <? super T> ：被称作有下界的通配符。

### 向上转型介绍
一个子类对象赋值给其父类的引用，这也叫向上转型。

```
//ArrayList是List的子类，可以赋值给父类引用
List<Integer> list = new ArrayList<Integer>();

// 编译错误，在一般泛型中，不能向上转型。
ArrayList<Number> list02 = new ArrayList<Integer>();

```

以下例子说明不允许把 ArrayList< Integer >对象向上转型为 ArrayList< Number >； 
换而言之， ArrayList< Integer > 和 ArrayList< Number > 两者之间没有继承关系。
```java
public class GenericType {
    public static void main(String[] args) {  
       	// 创建一个 ArrayList<Integer> 集合
		ArrayList<Integer> integerList = new ArrayList<>();
		
		// 添加一个 Integer 对象
		integerList.add(new Integer(123));
		
		// “向上转型”为 ArrayList<Number>
		ArrayList<Number> numberList = integerList;
		
		// 添加一个 Float 对象，Float 也是 Number 的子类，编译器不报错
		numberList.add(new Float(12.34));
		
		// 从 ArrayList<Integer> 集合中获取索引为 1 的元素（即添加的 Float 对象）：
		Integer n = integerList.get(1); // ClassCastException，运行出错
    }  
}

```

### 为什么需要泛型通配符

```java
public class Pair<T> {
    private T first;
    private T last;

    public Pair(T first, T last) {
        this.first = first;
        this.last = last;
    }
    public T getFirst() {
        return first;
    }
    public T getLast() {
        return last;
    }
    public void setFirst(T first) {
        this.first = first;
    }
    public void setLast(T last) {
        this.last = last;
    }
}


public class PairHelper {
    static int addPair(Pair<Number> p) {
        Number first = p.getFirst();
        Number last = p.getLast();
        return first.intValue() + last.intValue();
    }
}


public class Main {
    public static void main(String[] args) {
        Pair<Number> pair = new Pair<>(1, 2);
        int sum = PairHelper.addPair(pair);
    }
}

```

从需求中我们应该把List<Integer>传给PairHelper，但是在一般泛型中，不能向上转型。
所以这时候就需要泛型通配符


### 上界通配符 <? extends T>

逻辑上可以将 ArrayList<? extends Number> 看做是 ArrayList< Integer > 的父类，
因此，在使用了上界通配符 <? extends Number> 后，便可以将 ArrayList< Integer > 对象向上转型了。
> 但是，创建了一个 ArrayList<? extends Number> 集合 list，但我们并不能往 list 中添加 Integer、Float 等对象，这也说明了 list 集合并不是某个确定了数据类型的集合。
> 以防止在获取 ArrayList<? extends Number> 集合中元素的时候，产生 ClassCastException 异常。
```java
public class GenericType {
    public static void main(String[] args) {
        // 编译错误
		ArrayList<Number> list01 = new ArrayList<Integer>();
        // 编译正确，正确向上转型
		ArrayList<? extends Number> list02 = new ArrayList<Integer>();
        // 编译错误
        list02.add(new Integer(1));
        // 编译错误
        list02.add(new Float(1.0));
        // 编译正确, null属于任何类型
        list02.add(null);
    }  
}

```

简单使用
```java
// 改写前
public class PairHelper {
    static int addPair(Pair<Number> p) {
        Number first = p.getFirst();
        Number last = p.getLast();
        return first.intValue() + last.intValue();
    }
}

// 改写后
public class PairHelper {
    static int addPair(Pair<? extends Number> p) {
        Number first = p.getFirst();
        Number last = p.getLast();
        return first.intValue() + last.intValue();
    }
}

```

> 一句话总结：使用 extends 通配符表示可以读，不能写。

### 下界通配符 <? super T>

下界通配符 <? super T>：T 代表了类型参数的下界，<? super T>表示类型参数的范围是 T 和 T 的超类，直至 Object。
需要注意的是： <? super T> 也是一个数据类型实参，它和 Number、String、Integer 一样都是一种实际的数据类型。

简单使用
```java
public class GenericType {
    public static void main(String[] args) {
        ArrayList<Integer> list01 = new ArrayList<Number>();// 编译错误

        ArrayList<? super Integer> list02 = new ArrayList<Number>();// 编译正确

        ArrayList<? super Number> list = new ArrayList<>();

        list.add(new Integer(1));// 编译正确
        list.add(new Float(1.0));// 编译正确

        // Object 是 Number 的父类 
        list.add(new Object());// 编译错误
    }
}

```

> 其原因是， ArrayList<? super Number> 的下界是 ArrayList< Number > 。因此，我们可以确定 Number 类及其子类的对象自然可以加入 ArrayList<? super Number> 集合中； 
> 而 Number 类的父类对象就不能加入 ArrayList<? super Number> 集合中了，因为不能确定 ArrayList<? super Number> 集合的数据类型。

错误用法

```java
public class Test {
    public static void main(String[] args) {
    	// 创建一个 ArrayList<Integer> 集合
        ArrayList<Integer> list = new ArrayList<>();
        list.add(new Integer(1));
        // 调用 fillNumList() 方法，传入 ArrayList<Integer> 集合
        fillNumList(list);// 编译错误
    }

    public static void fillNumList(ArrayList<? super Number> list) {
        list.add(new Integer(0));// 编译正确
        list.add(new Float(1.0));// 编译正确
		
		// 遍历传入集合中的元素，并赋值给 Number 对象；会编译错误
        for (Number number : list) {
            System.out.print(number.intValue() + " ");
            System.out.println();
        }
        // 遍历传入集合中的元素，并赋值给 Object 对象；可以正确编译
        // 但只能调用 Object 类的方法，不建议这样使用
        for (Object obj : list) {
            System.out.println(obj);
        }
    }
}

```

> 一句话总结：使用 super 通配符表示可以写，不能读。


### 无限定通配符 <?>

无界通配符<?>：? 代表了任何一种数据类型，能代表任何一种数据类型的只有 null。需要注意的是： <?> 也是一个数据类型实参，它和 Number、String、Integer 一样都是一种实际的数据类型。

> Object 本身也算是一种数据类型，但却不能代表任何一种数据类型，所以 ArrayList< Object > 和 ArrayList<?> 的含义是不同的，
> 前者类型是 Object，也就是继承树的最高父类，而后者的类型完全是未知的；ArrayList<?> 是 ArrayList< Object > 逻辑上的父类。

简单使用

```java

public class GenericType {
	public static void main(String[] args) {

        ArrayList<Integer> list01 = new ArrayList<>(123, 456);
        ArrayList<?> list02 = list01; // 安全地向上转型
        
        ArrayList<?> list = new ArrayList<>();
        list.add(null);// 编译正确
        Object obj = list.get(0);// 编译正确

		list.add(new Integer(1));// 编译错误
		Integer num = list.get(0);// 编译错误
    }
}

```

> 大多数情况下，可以用类型参数 < T > 代替 <?> 通配符。



***

## 引用
[https://blog.csdn.net/weixin_45395059/article/details/126006369](https://blog.csdn.net/weixin_45395059/article/details/126006369)









