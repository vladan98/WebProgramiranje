package beans;

import java.util.Date;

public class Reservation {
	
	private String apartmentId;
	private Date startDate;
	private int nights;
	private double price;
	private String message;
	private String guestId;
	private ReservationStatus status;
	
	public Reservation() {}
	
	public Reservation(String apartmentId, Date startDate, int nights, double price, String message, String guestId,
			ReservationStatus status) {
		super();
		this.apartmentId = apartmentId;
		this.startDate = startDate;
		this.nights = nights;
		this.price = price;
		this.message = message;
		this.guestId = guestId;
		this.status = status;
	}

	public String getApartmentId() {
		return apartmentId;
	}

	public void setApartmentId(String apartmentId) {
		this.apartmentId = apartmentId;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public int getNights() {
		return nights;
	}

	public void setNights(int nights) {
		this.nights = nights;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getGuestId() {
		return guestId;
	}

	public void setGuestId(String guestId) {
		this.guestId = guestId;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public void setStatus(ReservationStatus status) {
		this.status = status;
	}
	
	
	
}
