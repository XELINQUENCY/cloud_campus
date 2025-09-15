package entity.library;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户基类 (抽象类)
 * 包含所有用户类型的通用属性。
 * 实现 Serializable 接口以便在网络上传输。
 */
@Setter
@Getter
@NoArgsConstructor
public class LibraryProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // 序列化版本号

    private int id;
    private String mainUserId;
    private boolean isOverdue;   // 是否有逾期记录
    private double fineAmount;  // 累计罚款金额

}