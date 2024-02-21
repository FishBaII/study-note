
## 构建自定义类加载器
```java

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class TestClassLoader extends URLClassLoader {

    private final Class<?>[] filteredClasses;

    public TestClassLoader(ClassLoader parent, Class<?> ...filteredClasses) {
        super(getClassPath(), parent);
        this.filteredClasses = filteredClasses;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        for (Class<?> filteredClass : filteredClasses) {
            if (filteredClass.getName().equals(name)) {
                Class<?> c = this.findLoadedClass(name);
                if (c == null) {
                    c = this.findClass(name);
                }

                if (resolve) {
                    this.resolveClass(c);
                }

                return c;
            }
        }
        return super.loadClass(name, resolve);
    }

    private static URL[] getClassPath() {
        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);
        URL[] result = new URL[entries.length];
        try {
            for (int i = 0; i < entries.length; i++) {
                result[i] = Paths.get(entries[i]).toAbsolutePath().toUri().toURL();
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException();
        }
        return result;
    }
}

```

## 使用自定义类加载器加载类并获取指定field值

```JAVA
public Object getClassField(Class<?> loadClass, String fieldName){
    try(URLClassLoader urlClassLoader = new TestClassLoader(getClass().getClassLoader(), loadClass)){
        Class<?> aClass = urlClassLoader.loadClass(loadedClass.getName());
        Field declaredField = aClass.getDeclaredField(fieldName);
        declaredField.setAccessible(true);
        return declaredField.get(null);
    }
}

```

