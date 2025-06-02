
## synchronized

### 介绍
synchronized是Java中的关键字，是一种**同步锁**。它修饰的对象有以下几种： 
　　1. 修饰一个代码块，被修饰的代码块称为同步语句块，其作用的范围是大括号{}括起来的代码，作用的对象是调用这个代码块的对象； 
　　2. 修饰一个方法，被修饰的方法称为同步方法，其作用的范围是整个方法，作用的对象是调用这个方法的对象； 
　　3. 修饰一个静态的方法，其作用的范围是整个静态方法，作用的对象是这个类的所有对象； 
　　4. 修饰一个类，其作用的范围是synchronized后面括号括起来的部分，作用主的对象是这个类的所有对象。



### 修饰代码块

1. synchronized(this), 只同步该代码块被**同一对象实例**访问，其他对象实例访问该代码块**不阻塞**，其他线程仍然可以访问该对象中的非synchronized(this)同步代码块
2. synchronized(obj)，给obj加锁，同一时刻只能一个线程访问该代码块，无论是否是同一实例

### 修饰非静态方法

相当于synchronized(this)，只锁当前对象

### 修饰静态方法

因为静态方法是属于类的而不是对象的，所以synchronized修饰的静态方法锁定的是这个类的所有对象

### 修饰类

synchronized作用于一个类时，是给这个类加锁，这个类的所有对象用的是同一把锁。

```java
class Account{
   public void method() {
      synchronized(Account.class) {
         // todo
      }
   }
}
```

## ReentrantLock

### 介绍
ReentrantLock是Java在JDK1.5引入的显式锁，在实现原理和功能上都和内置锁(synchronized)上都有区别
ReentrantLock是基于AQS实现的, 属于乐观锁
>- AQS即AbstractQueuedSynchronizer的缩写，这个是个内部实现了两个队列的抽象类，分别是同步队列和条件队列。
其中同步队列是一个双向链表，里面储存的是处于等待状态的线程，正在排队等待唤醒去获取锁，而条件队列是一个单向链表，
里面储存的也是处于等待状态的线程，只不过这些线程唤醒的结果是加入到了同步队列的队尾，AQS所做的就是管理这两个队列里面线程之间的等待状态-唤醒的工作。


### 使用
Lock lock = new ReentrantLock();
公平锁：先来的线程先执行，吞吐量下降
非公平：后来的线程有可能先执行，可插队不一定按顺序，减少CPU唤醒线程开销；可能导致线程饿死

###

```java
//ReentrantLock是Java代码实现的锁，我们就必须先获取锁，然后在finally中正确释放锁。
public class Counter {
    private final Lock lock = new ReentrantLock();
    private int count;

    public void add(int n) {
        lock.lock();
        try {
            count += n;
        } finally {
            lock.unlock();
        }
    }
}
```


### 不同获取锁的方式

#### lock()

尝试获取锁，如果其他线程已经持有该锁，则一直阻塞直到可获取


#### tryLock(long time, TimeUnit unit)

tryLock(): 尝试获取锁，如果被其他线程占用则立即返回**false**，可使用参数设置阻塞超时时间

```
try {
    if (lock.tryLock(1, TimeUnit.SECONDS)) {
        try {
            // 锁内代码
        } finally {
            lock.unlock();
        }
    } else {
        // 锁获取失败
    }
} catch (InterruptedException e) {
    // 处理中断
}
```


#### lockInterruptibly() 

调用后一直阻塞到获得锁 但是接受中断信号



1. 如果锁被另一个线程保持，则出于线程调度目的，禁用当前线程，并且在发生以下两种情况之一以
前，该线程将一直处于休眠状态：
锁由当前线程获得；或者其他某个线程中断当前线程。 

2. 如果当前线程获得该锁，则将锁保持计数设置为 1。
如果当前线程：
在进入此方法时已经设置了该线程的中断状态；或者在等待获取锁的同时被中断。 

则抛出 InterruptedException，并且清除当前线程的已中断状态。
3. 在此实现中，因为此方法是一个显式中断点，所以要优先考虑响应中断，而不是响应锁的普通获取或
重入获取。


```java

public class TestLockInterruptibly
{

    // @Test
    public void test3() throws Exception
    {
        final Lock lock = new ReentrantLock();
        lock.lock();

        Thread t1 = new Thread(() -> {
            try {
                lock.lockInterruptibly();
            } catch (InterruptedException e) {
                System.out.println(Thread.currentThread().getName() + " interrupted.");
            }
        }, "child thread -1");

        t1.start();
        Thread.sleep(1000);

        t1.interrupt();

        Thread.sleep(1000000);
    }

    public static void main(String[] args) throws Exception
    {
        new TestLockInterruptibly().test3();
    }
}

//输出： child thread -1 interrupted.
```

> 子线程会响应中断interrupt()







