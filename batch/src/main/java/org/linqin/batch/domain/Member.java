package org.linqin.batch.domain;

import lombok.Data;

@Data
public class Member {

    private Long id;

    private String mobile;

    /**
     * 你之所以能 "直接调用" getClass() 是因为这段代码写在**实例方法 `toString()` 内部**，
     * Java 自动帮你隐含了 `this`。写 `getClass()` 完全等价于写 `this.getClass()`。
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", mobile=").append(mobile);
        sb.append("]");
        return sb.toString();
    }
}
