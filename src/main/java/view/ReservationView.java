package view;

import entity.library.Reservation;
import java.io.Serializable;

/**
 * 用于界面显示的预约记录视图模型
 */
public class ReservationView implements Serializable {
    private static final long serialVersionUID = 1L;

    private Reservation reservation;
    private String bookTitle;

    public ReservationView(Reservation reservation, String bookTitle) {
        this.reservation = reservation;
        this.bookTitle = bookTitle;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public String getBookTitle() {
        return bookTitle;
    }
}