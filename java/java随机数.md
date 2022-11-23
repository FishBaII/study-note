```
private static String getRandomCode(){
        int max = 100000;
        int min = 10000;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        return String.valueOf(s);
    }
```
