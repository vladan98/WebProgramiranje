package beans;

import java.util.UUID;

public class Comment {

	private String reviewerId;
	private String apartmentId;
	private String text;
	private int rate;
	
	public Comment() {}
	
	
	public Comment(String reviewerId, String apartmentId, String text, int rate) {
		super();
		this.reviewerId = reviewerId;
		this.apartmentId = apartmentId;
		this.text = text;
		this.rate = rate;
	}


	public String getReviewerId() {
		return reviewerId;
	}


	public void setReviewerId(String reviewerId) {
		this.reviewerId = reviewerId;
	}


	public String getApartmentId() {
		return apartmentId;
	}


	public void setApartmentId(String apartmentId) {
		this.apartmentId = apartmentId;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


	public int getRate() {
		return rate;
	}


	public void setRate(int rate) {
		this.rate = rate;
	}
	
	
}
