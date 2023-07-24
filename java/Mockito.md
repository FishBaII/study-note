# Mockito
Mockito库能够Mock对象、验证结果以及打桩(stubbing)。
> version >= 3.4.0
### Mock静态方法

* 静态方法，包含无参和有参方法
```
public class StaticUtils {

    private StaticUtils() {}

    public static List<Integer> range(int start, int end) {
        return IntStream.range(start, end)
          .boxed()
          .collect(Collectors.toList());
    }

    public static String name() {
        return "Baeldung";
    }
}
```

* 无参mock
```
@Test
void givenStaticMethodWithNoArgs_whenMocked_thenReturnsMockSuccessfully() {
    assertThat(StaticUtils.name()).isEqualTo("Baeldung");

    try (MockedStatic<StaticUtils> utilities = Mockito.mockStatic(StaticUtils.class)) {
        utilities.when(StaticUtils::name).thenReturn("Eugen");
        assertThat(StaticUtils.name()).isEqualTo("Eugen");
    }

    assertThat(StaticUtils.name()).isEqualTo("Baeldung");
}
```

* 有参mock
```
@Test
void givenStaticMethodWithArgs_whenMocked_thenReturnsMockSuccessfully() {
    assertThat(StaticUtils.range(2, 6)).containsExactly(2, 3, 4, 5);

    try (MockedStatic<StaticUtils> utilities = Mockito.mockStatic(StaticUtils.class)) {
        utilities.when(() -> StaticUtils.range(2, 6))
          .thenReturn(Arrays.asList(10, 11, 12));

        assertThat(StaticUtils.range(2, 6)).containsExactly(10, 11, 12);
    }

    assertThat(StaticUtils.range(2, 6)).containsExactly(2, 3, 4, 5);
}
```


