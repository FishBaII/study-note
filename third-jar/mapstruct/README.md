# MapStuct

Mapstruct 用于Java Bean之间的转换  
官方GitHub：[进入](https://github.com/mapstruct/mapstruct)  
官网：[进入](https://mapstruct.org/)  


## 基础使用

1. 创建两个model使用转换，字段名称及类型一致
```
public class Doctor {
    private int id;
    private String name;

    private String specialty;
	
	//忽略getter和setter方法
}
```

```
public class DoctorDto {
    private int id;
    private String name;

    private String specialty;
	
	//忽略getter和setter方法
}
```

2. 创建mapper接口，使用MapStruct的@Mapper将接口的方法自动转换为对应对象类型（根据入参，出参自动转换）
```
import org.mapstruct.Mapper;

@Mapper
public interface DoctorMapper {

	//MapStruct的工厂类方法，用于返回mapper实例
    DoctorMapper INSTANCE = Mappers.getMapper(DoctorMapper.class);

	//将Doctor对象转换为DoctorDto
    DoctorDto toDto(Doctor doctor);

}
```

3. 调用Mapper的toDto方法

```
        Doctor doctor = new Doctor();
        doctor.setId(1);
        doctor.setName("tom");
        doctor.setSpecialty("psychology");
        DoctorDto doctorDto = DoctorMapper.INSTANCE.toDto(doctor);

```