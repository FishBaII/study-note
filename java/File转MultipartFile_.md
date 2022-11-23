- maven引入

```
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-mock</artifactId>
	<version>2.0.8</version>
</dependency>
```

```
File oldFile = new File(path);
InputStream inputStream = new FileInputStream(oldFile);
MultipartFile multipartFile = new MockMultipartFile("file", oldFile.getName(), "text/plain", IOUtils.toByteArray(inputStream));
inputStream.close();
```

#File操作

- 判断文件的目录是否存在，如不则新建

```
if (!file.getParentFile().exists())
{
  file.getParentFile().mkdirs();
}
if (!file.exists())
{
  file.createNewFile();
}
```
