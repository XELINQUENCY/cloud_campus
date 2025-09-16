package entity.library;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 预约记录实体类
 */
@Getter
@Setter
public class Reservation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int reservationId;
    private String mainUserId;
    private int bookId;

    // 新增属性
    private Date reservationDate; // 预约日期
    private Date expirationDate;  // 预约失效日期 (当书可借后开始计算)
    private String status;        // 状态: "等待中" (waiting), "可借阅" (available), "已失效" (expired), "已完成" (completed)

    // 构造函数
    public Reservation() {
    }

	public int getReservationId() {
		return reservationId;
	}

	public void setReservationId(int reservationId) {
		this.reservationId = reservationId;
	}

	public String getMainUserId() {
		return mainUserId;
	}

	public void setMainUserId(String mainUserId) {
		this.mainUserId = mainUserId;
	}

	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public Date getReservationDate() {
		return reservationDate;
	}

	public void setReservationDate(Date reservationDate) {
		this.reservationDate = reservationDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
}
