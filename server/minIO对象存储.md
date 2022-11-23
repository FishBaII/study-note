
> - [官网下载](https://docs.min.io/cn/)



<a name="7f14f60e"></a>
## 运行minio服务

- 下载minio.exe
- 用cmd执行minio.exe server D:\software\minIO\path （地址为文件存储路径）
- 左下角创建存储桶,点击桶edit policy修改访问策略前缀为 _._

<a name="6777f44f"></a>
## gradle引入

```
implementation 'io.minio:minio:3.0.10'
```

<a name="d687e4bf"></a>
## yml配置

```
spring:
  servlet:
    multipart:
      enabled: true #开启文件上传
      max-file-size: 10MB #限制文件上传大小为10M

minio:
  endpoint: http://localhost:9000 #MinIO服务所在地址
  bucketName: lim-test #存储桶名称
  accessKey: minioadmin #访问的key
  secretKey: minioadmin #访问的秘钥
```

<a name="f33889b3"></a>
## api调用

```java
@RestController
@RequestMapping("minio")
public class MinioController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MinioController.class);
    @Value("${minio.endpoint}")
    private String ENDPOINT;
    @Value("${minio.bucketName}")
    private String BUCKET_NAME;
    @Value("${minio.accessKey}")
    private String ACCESS_KEY;
    @Value("${minio.secretKey}")
    private String SECRET_KEY;


    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public CommonResult upload(@RequestParam("file") MultipartFile file) {
        try {
            //创建一个MinIO的Java客户端
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESS_KEY, SECRET_KEY);
            boolean isExist = minioClient.bucketExists(BUCKET_NAME);
            if (isExist) {
                LOGGER.info("存储桶已经存在！");
            } else {
                //创建存储桶并设置只读权限
                minioClient.makeBucket(BUCKET_NAME);
                minioClient.setBucketPolicy(BUCKET_NAME, "*.*", PolicyType.READ_ONLY);
            }
            String filename = file.getOriginalFilename();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            // 设置存储对象名称
            String objectName = sdf.format(new Date()) + "/" + filename;
            // 使用putObject上传一个文件到存储桶中
            minioClient.putObject(BUCKET_NAME, objectName, file.getInputStream(), file.getContentType());
            //LOGGER.info("文件上传成功!");
            MinioUploadDto minioUploadDto = new MinioUploadDto();
            minioUploadDto.setName(filename);
            minioUploadDto.setUrl(ENDPOINT + "/" + BUCKET_NAME + "/" + objectName);
            return CommonResult.success(minioUploadDto);
        } catch (Exception e) {
            LOGGER.info("上传发生错误: {}！", e.getMessage());
        }
        return CommonResult.failed();
    }


    @RequestMapping(value = "/delete", method = RequestMethod.POST)

    public CommonResult delete(@RequestParam("objectName") String objectName) {
        try {
            MinioClient minioClient = new MinioClient(ENDPOINT, ACCESS_KEY, SECRET_KEY);
            minioClient.removeObject(BUCKET_NAME, objectName);
            return CommonResult.success(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CommonResult.failed();
    }
}

@Data
public class MinioUploadDto{
	private String url;
	private String name;
}
```
