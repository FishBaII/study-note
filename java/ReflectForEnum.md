## 需求场景说明

现有一个switch块，接受枚举类作为参数，根据不同枚举值做出不同处理
```java
//枚举类
public enum ProductType {

    COL,
    KOF,
    TNL

}

//工具方法
public class SwitchUtil {
    public static String getType(ProductType type){

        return switch (type) {
            case COL -> "col";
            case KOF -> "kof";
            case TNL -> "tnl";
            //当匹配枚举不存在，进行抛出异常等操作
            default -> "others";
        };

    }
}
```

需要通过junit测试用例对此工具方法进行代码覆盖，但是以上枚举值已全部包含在case判断中，即无法通过正常流程触发default处理方法


## 初步思路

* 传入其他类型数据
```java

@Test
void objectTypeTest(){

    Object object = new String("any");
    SwitchUtil.getType((ProductType) object);

}

//抛出异常java.lang.ClassCastException，无法将object转为ProductType类型
//switch也无法接受null参数

```
* powerMock

```
//todo
```

* 直接使用反射创建枚举实例

```java
    @Test
    void reflectSimpleTest() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {

        Class<ProductType> productTypeClass = ProductType.class;
        Constructor<ProductType> cp = productTypeClass.getConstructor(null);
        cp.setAccessible(true);
        cp.newInstance(null);

    }
    
```

> It is a compile-time error to attempt to explicitly instantiate an enum type (§15.9.1). 
> The final clone method in Enum ensures that enum constants can never be cloned, 
> and the special treatment by the serialization mechanism ensures that duplicate instances are never created as a result of deserialization. 
> Reflective instantiation of enum types is prohibited. Together, these four things ensure that no instances of an enum type exist beyond those defined by the enum constants.

所以为了保证枚举实例可以用 `==` 进行比较，反射相关方法也会对枚举进行判断筛选
```
        if ((clazz.getModifiers() & Modifier.ENUM) != 0)
            throw new IllegalArgumentException("Cannot reflectively create enum objects");
```


## 解决办法


### 使用unsafe类创建实例

```java
    @Test
    void test1() throws Exception {

        //使用反射获取unsafe实例
        Constructor< ? > constructor = Unsafe.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Unsafe unsafe = (Unsafe) constructor.newInstance();
        //绕过构造方法、初始化代码来非常规地创建对象
        ProductType enumValue = (ProductType) unsafe.allocateInstance(ProductType.class);


        Field valuesField = ProductType.class.getDeclaredField("$VALUES");
        makeAccessible(valuesField);
        // just copy old values to new array and add our new field.
        ProductType[] oldValues = (ProductType[]) valuesField.get(null);
        ProductType[] newValues = new ProductType[oldValues.length + 1];
        System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
        newValues[oldValues.length] = enumValue;
        valuesField.set(null, newValues);

        Field ordinalField = Enum.class.getDeclaredField("ordinal");
        //赋予ordinalField修改权限
        makeAccessible(ordinalField);
        //赋值枚举实例序号为3（0为起始值）
        ordinalField.setInt(enumValue, 3);
        //如果有其他字段需要赋值，重复 赋予修改权限 和 赋值 这两步
        //...
        //执行util方法，会进入swtich的default方法
        Assertions.assertEquals("others", SwitchUtil.getType(enumValue));

    }
```


### 修改默认访问修饰符

使用反射将final删除，使之可以被继承修改

```

    public static void makeAccessible(Field field) throws Exception {
        field.setAccessible(true);
        
        //JDK11及以下写法
        //Field modifiersField = Field.class.getDeclaredField("modifiers");
        
        //JDK12引入了模块化，无法直接获取部分field，会提示No such XXXX异常，需要通过getDeclaredFields0获取，还需要添加如下JVM参数
        //--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        Field modifiersField = null;
        for (Field each : fields) {
            if ("modifiers".equals(each.getName())) {
                modifiersField = each;
            }
        }
        
        //除去final限制
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~ Modifier.FINAL);
    }
```


添加JVM参数有以下方法

1. idea运行配置  
可于idea Run/Debug Configuration -> Add VM options 进行添加

2. 启动命令添加  
如：java -jar xxx.jar --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED

3. Maven配置  

```
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <argLine>
                        --add-opens=java.base/java.lang=ALL-UNNAMED 
                        --add-opens=java.base/java.lang.reflect=ALL-UNNAMED
                    </argLine>
                </configuration>
            </plugin>
```


## 引用参考

[https://blog.gotofinal.com/java/diorite/breakingjava/2017/06/24/dynamic-enum.html](https://blog.gotofinal.com/java/diorite/breakingjava/2017/06/24/dynamic-enum.html)


