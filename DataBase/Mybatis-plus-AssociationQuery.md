<a name="aeff888c"></a>
## 引入mybatis plus

<a name="b858a645"></a>
## model层

- [@TableField(exist ](/TableField(exist ) = false) 
- private List roles;

<a name="e3b537bd"></a>
## mapper层

```
	@Select("SELECT id, username, cnname, create_datetime FROM user_info LIMIT ${page}, ${size}")
    @Results(id="userMap",value = {
            @Result(id = true, property = "id",column = "id"),
            @Result(property = "cnname",column = "cnname"),
            @Result(property = "userName",column = "username"),
            @Result(property = "createTime", column = "create_datetime"),
            @Result(property = "password", column = "password"),
            @Result(property = "roles",column = "id",many = @Many(select = "com.example.demo.mapper.RoleMapper.getRolesByUserId"))

    })
    List<UserDTO> selectUserWithPage(int page, int size);
```

<a name="dd7768f4"></a>
## 一对一

- mapper： one = [@One(select ](/One(select ) = "mapper方法") 

<a name="138a6766"></a>
## 注意

- @Results要写在指定搜索的接口方法上，子节点映射类类型要与 @Many返回的类型一致
