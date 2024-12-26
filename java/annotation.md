# Annotation

----
## ConstraintValidator自定义参数校验
ConstraintValidator是Java Bean Validation（JSR-303）规范中的一个接口，用于实现自定义校验注解的校验逻辑。
ConstraintValidator定义了两个泛型参数，分别是注解类型和被校验的值类型。
在实现ConstraintValidator接口时，需要重写initialize、isValid等方法，并实现具体的校验逻辑。


### 项目demo

#### 需求

自定义一个注解，用于接口的字符串参数校验，防止同时传多个相同名称的参数（spring默认情况下传多个相同参数会以逗号为分隔符进行拼接）

#### 环境和依赖

| dependency | version |
|:----------:|:-------:|
|    JDK     |   17    |
| SpringBoot |  3.2.1  |
|   Maven    |   3.6   |

```xml

<!-- other dependency skip -->
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

```


#### 测试接口

```java
@RestController
@RequestMapping
public class HiController {

    //return the parameter value from request payload
    @GetMapping("/name")
    public String getName(String name){
        return name;
    }
}
```

>- 此时传多个相同参数，如 /name?name=tom&name=apple，返回 tom,apple

#### 创建自定义注解

```java
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DetectStringValidation.class})
@Documented
public @interface ParamMaxNumberCheck {

    String message() default "Do not pass duplicate parameters";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    //skip validation when value is empty
    boolean isPassIfEmpty() default true;

    String splitChar() default ",";

    //Customize repeatable items
    int maxSupportItems() default 1;

}
```


```java
public class DetectStringValidation implements ConstraintValidator<ParamMaxNumberCheck, String> {

    private Boolean isPassIfEmpty;

    private int maxSupportItems;

    private String splitChar;

    @Override
    public void initialize(ParamMaxNumberCheck constraintAnnotation) {
        this.isPassIfEmpty = constraintAnnotation.isPassIfEmpty();
        this.maxSupportItems = constraintAnnotation.maxSupportItems();
        this.splitChar = constraintAnnotation.splitChar();
    }

    //isPassIfEmpty is true and value is empty: skip validation
    //others: check the max support items
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return this.isPassIfEmpty && !StringUtils.hasText(s)
                || StringUtils.hasText(s) && validateValue(s);
    }


    boolean validateValue(String s){

        String[] splitValues = s.split(splitChar);
        return splitValues.length <= maxSupportItems;

    }
}
```

#### 测试自定义注解

1. 添加spring校验声明注解及自定义注解

```java
@RestController
@RequestMapping
//add spring validation annotation
@Validated
public class HiController {

    //return the parameter value from request payload
    @GetMapping("/name")
    public String getName(@ParamMaxNumberCheck String name){
        return name;
    }
}
```

>- 如果不添加@Validated注解，会返回bad request错误，不会打印相关错误日志

2. 接口测试

访问/name?name=tom&name=apple，返回结果如下：

```json
{
  "timestamp": "2024-12-10T10:04:19.040+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/name"
}
```

日志打印相关错误如下：

```
jakarta.validation.ConstraintViolationException: getName.name: Do not pass duplicate parameters
xxxx
xxxx

```

#### 增加异常处理方法

通常我们需要捕获并处理相关异常， 自定义校验不通过会抛出ConstraintViolationException异常。

1. 新增全局异常处理类

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Object handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e){
        return e.getMessage();
    }

}
```

2. 接口测试

访问/name?name=tom&name=apple，返回结果如下：

```
getName.name: Do not pass duplicate parameters
```

>- 示例的异常捕获返回仅作测试，实际项目中的异常处理请使用自定义的response dto返回此异常



## 启用lib的bean

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({MyBean.class})
public @interface EnableLibBean {
}
```

>- 引入此lib后使用@EnableLibBean来启用Mybean