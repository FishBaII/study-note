# Day 1：Java 集合框架深度复习

> 计划日期：Week 1 Day 1 | 主题：HashMap、ConcurrentHashMap、ArrayList
> 输出要求：手写 HashMap get/put 伪代码

---

## 一、核心概念速览

### 1.1 HashMap 原理（JDK 1.7 vs 1.8）

| 维度 | JDK 1.7 | JDK 1.8 |
|------|---------|---------|
| 数据结构 | 数组 + 链表 | 数组 + 链表 + **红黑树** |
| 插入方式 | 头插法（多线程下可能死循环） | **尾插法**（解决死循环） |
| Hash 计算 | 4 次扰动 + 异或 | 1 次扰动（高 16 位异或低 16 位） |
| 扩容时机 | size >= threshold && table[i] != null | size > threshold |
| 扩容后位置 | 重新 hash | 原位置 i 或 i + oldCap（更高效） |

**默认参数：**
- 初始容量 16，负载因子 0.75
- 链表转红黑树阈值：8（链表长度 >= 8 且数组长度 >= 64）
- 红黑树退化为链表阈值：6

### 1.2 ConcurrentHashMap

**JDK 1.7 → 分段锁（Segment）**
- 默认 16 个 Segment，每个 Segment 持有一把小 ReentrantLock
- 并发度 = Segment 数量，理论最大 16 个线程同时写入

**JDK 1.8 → CAS + synchronized**
- 放弃 Segment，直接用数组 + 链表/红黑树
- 写入：CAS 尝试设置空桶 → 失败则 synchronized 锁住头节点
- 扩容：支持多线程协作扩容（`transfer` 方法按步长分配迁移区间）
- `size()`：使用 `CounterCell` 数组分散计数，避免 CAS 自旋竞争

### 1.3 ArrayList vs LinkedList

| 维度 | ArrayList | LinkedList |
|------|-----------|------------|
| 底层结构 | `Object[]` 数组 | 双向链表 |
| 随机访问 | O(1) | O(n) |
| 头/尾插入 | O(n)（需要移动元素） | O(1) |
| 中间插入 | O(n) | O(1)（定位后） |
| 内存占用 | 连续内存，尾部有预留空间 | 每个节点多两个指针开销 |
| 默认容量 | 10 | 无预分配 |

> **一句话选型**：读多写少用 ArrayList，频繁头/中插入删除用 LinkedList。

---

## 二、面试高频题（带答案）

### Q1：HashMap 的 put 过程（源码级描述）

**JDK 1.8 put 流程：**

```
1. 计算 hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16)
2. 判断 table 是否为空，是则 resize() 初始化
3. 计算桶位置 index = (n - 1) & hash
4. 若 table[index] == null，直接 CAS/赋值 newNode，跳步骤 7
5. 若 table[index].hash == hash && key 相等（== 或 equals），覆盖旧值
6. 若不是：
   a. 若是 TreeNode → 走红黑树插入
   b. 若是链表：
      - 遍历链表，找到相同 key 则覆盖
      - 未找到则尾插新节点
      - 检查链表长度 >= 8 → treeifyBin()（数组 >= 64 才真正树化）
7. 检查 ++size > threshold → resize() 扩容
```

### Q2：HashMap 为什么容量是 2 的幂？

1. **高效取模**：`(n - 1) & hash` 等价于 `hash % n`，但位运算快得多
2. **均匀分布**：n 为 2 的幂时，`n-1` 的二进制全是 1，hash 的每一位都能参与索引计算
3. **扩容时高效迁移**：新位置只可能是 i 或 i + oldCap，不需要重新 hash

### Q3：ConcurrentHashMap 如何保证线程安全？

- **put**：空桶用 CAS 尝试置入；非空桶用 `synchronized` 锁住头节点
- **get**：无锁，利用 volatile 保证可见性
- **扩容**：多线程协作，每个线程领取一个迁移区间（`transferIndex` 用 CAS 分配）
- **计数**：`CounterCell` 数组 + `baseCount`，用 CAS 更新，避免竞争

### Q4：为什么 HashMap 线程不安全？有哪些表现？

1. **JDK 1.7 头插法导致死循环**：扩容时链表反转，多线程并发可能形成环形链表，get 时 CPU 100%
2. **数据覆盖**：两个线程同时 put 到同一空桶，后一个可能覆盖前一个
3. **size 不准确**：`size++` 不是原子操作，可能少计

### Q5：HashSet 底层是什么？怎么保证不重复？

HashSet 底层就是 **HashMap**，value 固定为 `new Object()`（PRESENT）。
- `add(E e)` → `map.put(e, PRESENT) == null`
- 利用 HashMap 的 key 唯一性保证不重复

---

## 三、手写 HashMap get/put 伪代码

### 简化版 HashMap 实现（JDK 1.8 风格）

```java
public class MyHashMap<K, V> {
    // 默认容量 16，必须是 2 的幂
    static final int DEFAULT_CAPACITY = 16;
    static final float LOAD_FACTOR = 0.75f;
    
    Node<K, V>[] table;
    int size;
    int threshold; // 扩容阈值 = capacity * loadFactor
    
    static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;
        
        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }
    
    // ---------- hash 函数 ----------
    // 高16位异或低16位，减少hash冲突（扰动函数）
    static int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }
    
    // ---------- get ----------
    public V get(Object key) {
        int hash = hash(key);
        // 1. 定位桶
        int index = (table.length - 1) & hash;
        Node<K, V> first = table[index];
        
        // 2. 检查头节点
        if (first == null) {
            return null;
        }
        if (first.hash == hash && (first.key == key || key.equals(first.key))) {
            return first.value;
        }
        
        // 3. 遍历链表（或树）
        Node<K, V> current = first.next;
        while (current != null) {
            if (current.hash == hash && (current.key == key || key.equals(current.key))) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }
    
    // ---------- put ----------
    public V put(K key, V value) {
        if (table == null || table.length == 0) {
            table = new Node[DEFAULT_CAPACITY];
            threshold = (int) (DEFAULT_CAPACITY * LOAD_FACTOR);
        }
        
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        Node<K, V> head = table[index];
        
        // Case 1: 空桶 → 直接放
        if (head == null) {
            table[index] = new Node<>(hash, key, value, null);
            size++;
            if (size > threshold) resize();
            return null;
        }
        
        // Case 2: 头节点 key 相同 → 覆盖
        if (head.hash == hash && (head.key == key || key.equals(head.key))) {
            V oldValue = head.value;
            head.value = value;
            return oldValue;
        }
        
        // Case 3: 遍历链表（省略红黑树情况）
        Node<K, V> current = head;
        while (true) {
            // key 相同 → 覆盖
            if (current.hash == hash && (current.key == key || key.equals(current.key))) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            // 到达链表尾部 → 尾插新节点
            if (current.next == null) {
                current.next = new Node<>(hash, key, value, null);
                size++;
                if (size > threshold) resize();
                return null;
            }
            current = current.next;
        }
    }
    
    // ---------- resize 扩容 ----------
    void resize() {
        int oldCap = table.length;
        int newCap = oldCap << 1;           // 容量翻倍
        threshold = (int) (newCap * LOAD_FACTOR);
        
        Node<K, V>[] newTable = new Node[newCap];
        
        for (int i = 0; i < oldCap; i++) {
            Node<K, V> node = table[i];
            if (node == null) continue;
            
            // 将原链表拆分为两个子链表：低位（留在原索引）和高位（移到 i + oldCap）
            Node<K, V> loHead = null, loTail = null;  // 低位链表
            Node<K, V> hiHead = null, hiTail = null;  // 高位链表
            
            while (node != null) {
                // hash & oldCap == 0 → 索引不变
                // hash & oldCap != 0 → 索引变为 i + oldCap
                if ((node.hash & oldCap) == 0) {
                    if (loTail == null) loHead = node;
                    else loTail.next = node;
                    loTail = node;
                } else {
                    if (hiTail == null) hiHead = node;
                    else hiTail.next = node;
                    hiTail = node;
                }
                node = node.next;
            }
            
            if (loTail != null) { loTail.next = null; newTable[i] = loHead; }
            if (hiTail != null) { hiTail.next = null; newTable[i + oldCap] = hiHead; }
        }
        table = newTable;
    }
}
```

### 手写核心要点总结（面试时口述/白板画图）

```
put 流程（5 步）：
① hash = key.hashCode() ^ (key.hashCode() >>> 16)
② index = (n-1) & hash，定位桶
③ 空桶 → 直接 newNode
④ 桶非空：
   - key 相同 → 覆盖 value（先判断头节点，再遍历）
   - key 不同 → 尾插（JDK 1.8）→ 超 8 且容量 ≥ 64 则树化
⑤ 检查 size > threshold → resize()

get 流程（3 步）：
① hash → index 定位桶
② 判断头节点是否为 null？null 返回 null
③ 头节点非 null → 先比头节点 → 再遍历（先判断 hash，再判断 == 或 equals）

resize 流程：
① 容量 ×2，threshold ×2
② 遍历旧数组每个桶
③ 每个桶的节点用 (hash & oldCap) == 0 分两类
   - 0 → 留在原索引 i
   - 非 0 → 移到 i + oldCap
④ JDK 1.8 的优化：不需要重新 hash，只需一位判断
```

---

## 四、实战练习

### 练习 1：模拟 HashMap 查找过程

```java
// 假设 HashMap 初始容量 16
// 问：以下操作后，table[1] 的链表结构是什么？

Map<String, Integer> map = new HashMap<>(16);
map.put("key1", 1);  // 假设 hash("key1") & 15 = 1
map.put("key2", 2);  // 假设 hash("key2") & 15 = 1（冲突!）
map.put("key1", 100); // 覆盖
map.get("key2");     // 返回 ?
```

<details>
<summary>答案</summary>

```
table[1]: Node("key1", 100) → Node("key2", 2) → null
get("key2") 返回 2

解析：
1. put("key1") → 桶空，插入头节点
2. put("key2") → hash 冲突，尾插到链表末尾（JDK 1.8）
3. put("key1") → key 相同，覆盖 value 为 100，链表结构不变
4. get("key2") → 遍历到第二个节点，返回 2
```
</details>

### 练习 2：扩容迁移计算

```java
// 旧容量 oldCap = 16 (二进制 10000)
// 旧索引 index = 5

// 对于以下 hash 值，扩容后分别放在哪个索引？
int hash1 = 5;   // 二进制 0...0101
int hash2 = 21;  // 二进制 0...10101

// 判断条件：(hash & oldCap) == 0 ?
```

<details>
<summary>答案</summary>

```
hash1 = 5  →  5 & 16 = 0        → (hash & oldCap) == 0 → 留在索引 5
hash2 = 21 → 21 & 16 = 16 != 0  → (hash & oldCap) != 0 → 移到索引 5 + 16 = 21

验证：newCap = 32
  5  % 32 = 5   ✓
  21 % 32 = 21  ✓
  
关键原理：
  hash & oldCap 就是在判断 hash 的第 5 位（oldCap=16=2^4，对应第 5 位）
  第 5 位 = 0 → 索引不变
  第 5 位 = 1 → 索引 + oldCap
```
</details>

### 练习 3：手写 LRU 缓存（LinkedHashMap 实现）

```java
// 用 LinkedHashMap 实现一个最大容量为 3 的 LRU 缓存
// 要求：实现 get 和 put 方法

class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;
    
    public LRUCache(int maxSize) {
        // accessOrder=true → 按访问顺序排序（get 过的放末尾）
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }
    
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;  // 超过最大容量时淘汰最老的
    }
}
```

---

## 五、易错点/坑

1. **HashMap 线程不安全，别在并发环境裸用** → 改用 `ConcurrentHashMap`
2. **HashMap key 尽量用不可变对象**（如 String、Integer），如果用可变对象且改了 hashCode，get 不到
3. **ConcurrentHashMap 的 key/value 不能为 null**（与 HashMap 不同）
4. **ArrayList 不要在 foreach 中 remove** → 用 Iterator 或 `removeIf`
5. **ArrayList 默认容量 10**，预估好大小用 `new ArrayList<>(size)` 避免频繁扩容

---

## 六、今日自测 Checklist

- [ ] 能口述 HashMap put 流程（源码级 5 步）
- [ ] 能口述 HashMap get 流程（3 步）
- [ ] 能解释 JDK 1.7 → 1.8 的 4 个变化
- [ ] 能解释为什么容量是 2 的幂
- [ ] 能写出扩容时 `(hash & oldCap) == 0` 的判断逻辑
- [ ] 能解释 ConcurrentHashMap 1.7 分段锁和 1.8 CAS+synchronized 的区别
- [ ] 能说出 ArrayList vs LinkedList 的选型依据
- [ ] 能手写简化版 HashMap 的 get/put/resize

---

> **Day 1 完成！** 明天 Day 2：并发编程（线程池、synchronized、Lock、volatile）