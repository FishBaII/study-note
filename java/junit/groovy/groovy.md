
### lib

```xml
        <dependency>
            <groupId>org.spockframework</groupId>
            <artifactId>spock-spring</artifactId>
            <version>2.4-M4-groovy-4.0</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.spockframework</groupId>
            <artifactId>spock-core</artifactId>
            <version>2.4-M4-groovy-4.0</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-all</artifactId>
            <version>2.4.21</version>
            <scope>test</scope>
        </dependency>
```


### demo

```java
import spock.lang.Specification


class DemoTest extends Specification{

    private CommonService commonService
    myMapper mapper = Mock()

    def setup(){
        commonService = new CommonService(mapper)
    }

    def "test name 1"(){
        given:
        Account account = new Account()
        and:
        //mock mapper
        mapper.getAccount(_, _) >> account
        when:
        def accountResult = commonService.getAccountById(1)
        then:
        accountResult != null
        accountResult.id == account.id
    }
}
```

