<a name="8df3caf8"></a>
## mapper代码

```
@Repository
public interface TableMapper {

    //获取指定数据库的所有表
    @Select("select * from information_schema.TABLES where TABLE_SCHEMA=#{schema}")
    List<Map> listTable(String schema);

    //获取指定表的所有信息
    @Select("select * from information_schema.COLUMNS where TABLE_SCHEMA = #{schema} and TABLE_NAME=#{tableName}")
    List<Map> listTableColumn(String schema,String tableName);

    //todo
    @DS("#session.tenantName")
    @Select("select * ")
    List<Map> listSelect(String schema,String tableName);


    //插入单条数据
    //@param tableName 表名
    //@param keyList   字段名List
    //@param valueList  插入值List
    @Insert({"<script>",
            "insert into ${tableName} ",
            "        <if test=' keyList != null '> " ,
            "            <foreach collection='keyList' index='index' item='key' open='(' close=')' separator=','>" ,
            "                ${key}" ,
            "            </foreach>" ,
            "        </if>" ,
            "        values " ,
            "        <if test=' valueList != null '> " ,
            "           <foreach collection='valueList' index='index' item='value' open='(' close=')' separator=','>" ,
            "               &apos;${value}&apos;" ,
            "           </foreach>" ,
            "       </if>" ,
            "</script>"})
    void insertByList(List keyList, List valueList, String tableName);


    //删除数据
    //@param tableName  表名
    //@param customerMap key为筛选条件字段，value为筛选条件字段的值
    @Delete({"<script>" ,
            "DELETE FROM ${tableName}" ,
            "where",
            "<if test='customerMap != null'>" ,
            "<foreach collection='customerMap.entrySet()' index='key' item='value' separator='AND'>",
            "${key}= &apos;${value}&apos;",
            "</foreach>",
            "</if>",
            "</script>"})
    void deleteByColum(Map customerMap,String tableName);

    //修改数据
    //@param tableName  表名
    //@param columMap  key为修改的字段，value为修改的字段值
    //@param customerMap key为筛选条件字段，value为筛选条件字段的值
    @Update({"<script>" ,
            "update ${tableName}",
            "set",
            "<if test='columMap != null'>",
            "<foreach collection='columMap.entrySet()' index='key' item='value' separator=','>",
            "${key}= &apos;${value}&apos;",
            "</foreach>",
            "</if>",
            "where " ,
            "<if test='customerMap != null'>",
            "<foreach collection='customerMap.entrySet()' index='key' item='value' separator='and'>",
            "${key}= &apos;${value}&apos;",
            "</foreach>",
            "</if>",
            "</script>"})
    void upadteByMap(Map columMap, Map customerMap, String tableName);


    //参数查询
    //columList为查询字段名List
    //
    <select id="selectTemplateWithAllColumns" resultType="java.util.LinkedHashMap" >
		select  *  from	${tableName}
		<where>
			<if test='conditionMap!=null'>
				and
				<foreach collection='conditionMap.entrySet()' index='condition' item='value' separator='and'>
					${condition}= #{value}
				</foreach>
			</if>
			<if test="likeConditionMap != null">
				and
				<foreach collection="likeConditionMap.entrySet()" index="condition" item="value" separator="and">
					${condition} like &apos;%${value}%&apos;
				</foreach>
			</if>
			<if test="timeConditionMap != null">
				and
				<foreach collection="timeConditionMap.entrySet()" index="condition" item="timeList" separator="and">
					<foreach collection="timeList" index="index" item="timeValue" separator="and">
						<if test="index == 0 and timeValue != null and timeValue != ''">
							condition &gt;= timeValue
						</if>
						<if test="index == 1 and timeValue != null and timeValue != ''">
							condition &lt;= timeValue
						</if>
					</foreach>
				</foreach>
			</if>
		</where>
	</select>
```

> mapper动态sql需加{""}<br />
value值需加 两个  ' 单引号修饰<br />
collection='map.entrySet()'

