https://juejin.cn/post/6956190395319451679

class: @Mapper(imports = xxxUtil.class)
@Name("xxx")
static String xxx{}

method: @Mapping(target = "version", constant = xxxVo.VERSION)
@Mapping(target = "birthDate", expression = "java(xxxUtils.getTime())")

@Named("q1")
static String q1(String text){}

@Mapping(target = "text", source="text", qualifiedByName="q1")