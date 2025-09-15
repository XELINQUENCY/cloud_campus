package entity.shop;

import java.io.Serializable;

public class Address implements Serializable {
	private static final long serialVersionUID = 1L;
	private String addr;
	private String houseNumber;
	private String name;
	private String phoneNumber;
	
	public String getAddr() { return addr; }
	public String getHouseNumber() { return houseNumber; }
	public String getName() { return name; }
	public String getPhoneNumber() { return phoneNumber; }
	public void setAddr(String addr) { this.addr = addr; }
    public void setHouseNumber(String houseNumber) { this.houseNumber = houseNumber; }
    public void setName(String name) { this.name = name; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
