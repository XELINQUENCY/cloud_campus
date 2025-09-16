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
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getMainUserId() {
		return mainUserId;
	}
	public void setMainUserId(String mainUserId) {
		this.mainUserId = mainUserId;
	}
	public boolean isOverdue() {
		return isOverdue;
	}
	public void setOverdue(boolean isOverdue) {
		this.isOverdue = isOverdue;
	}
	public double getFineAmount() {
		return fineAmount;
	}
	public void setFineAmount(double fineAmount) {
		this.fineAmount = fineAmount;
	}
	
    

}