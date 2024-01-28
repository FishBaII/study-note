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



## 使用unsafe类创建实例

```
    @Test
    void test1() throws Exception {


        Constructor< ? > constructor = Unsafe.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        Unsafe unsafe = (Unsafe) constructor.newInstance();
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
        makeAccessible(ordinalField);
        ordinalField.setInt(enumValue, 3);
        Assertions.assertEquals("others", SwitchUtil.getType(enumValue));

    }
```


## 更多的反射

```
//添加jvm参数 -add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED
    public static void makeAccessible(Field field) throws Exception {
        field.setAccessible(true);
        //Field modifiersField = Field.class.getDeclaredField("modifiers");
        Method getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
        getDeclaredFields0.setAccessible(true);
        Field[] fields = (Field[]) getDeclaredFields0.invoke(Field.class, false);
        Field modifiersField = null;
        for (Field each : fields) {
            if ("modifiers".equals(each.getName())) {
                modifiersField = each;
            }
        }
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~ Modifier.FINAL);
    }
```
