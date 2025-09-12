package entity.library;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 借阅记录实体类
 */
@Getter
@Setter
public class BorrowRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // Getter 和 Setter 方法
    private int recordId;
    private String mainUserId;
    private int copyId;
    private Date borrowDate;  // 借出日期
    private Date dueDate;     // 应还日期
    private Date returnDate;  // 实际归还日期 (如果未还，则为 null)

    // 新增属性
    private int renewalCount; // 续借次数
    private boolean isOverdue;  // 该次借阅是否已逾期
    private double fineAmount;  // 本次产生的罚款

    // 构造函数
    public BorrowRecord() {
    }

    public void setIsOverdue(int isOverdue) {
        this.isOverdue = isOverdue != 0;
    }

    public void setIsOverdue(boolean isOverdue) {
        this.isOverdue = isOverdue;
    }

}
