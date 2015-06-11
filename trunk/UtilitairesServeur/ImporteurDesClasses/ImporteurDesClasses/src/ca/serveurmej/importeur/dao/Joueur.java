/**
 * Simple JAVA POJO to insert in DB in the tables 
 * jos_user and jos_comprofiler
 */
package ca.serveurmej.importeur.dao;

import java.sql.Date;

/**
 * @author JohnI
 *
 */
public class Joueur implements IJoueur {
	private int id;
	private String firstname;
	private String middlename;
	private String lastname;
	private String name;
	private String username;
	private String registeripaddr;
	private String email;
	private String password;
	private String usertype;
	private String block;
	private String sendmail;
	private int acceptedterms;
	private int gid;
	private Date registerDate;
	private String admin_language;
	private String language;
	private String editor;
	private String helpsite;
	private String timezone;
	private String cb_gradelevel;
	private String cb_gender;
	private String cb_school;
	private String cb_country;
	private String cb_province;
	
	
	
	
	public Joueur(int id, String firstname, String middlename, String lastname,
			String name, String username, String registeripaddr, String email,
			String password, String usertype, String block, String sendmail,
			int acceptedterms, int gid, Date registerDate,
			String admin_language, String language, String editor,
			String helpsite, String timezone, String cb_gradelevel,
			String cb_gender, String cb_school, String cb_country,
			String cb_province) {
		
		super();
		this.id = id;
		this.firstname = firstname;
		this.middlename = middlename;
		this.lastname = lastname;
		this.name = name;
		this.username = username;
		this.registeripaddr = registeripaddr;
		this.email = email;
		this.password = password;
		this.usertype = usertype;
		this.block = block;
		this.sendmail = sendmail;
		this.acceptedterms = acceptedterms;
		this.gid = gid;
		this.registerDate = registerDate;
		this.admin_language = admin_language;
		this.language = language;
		this.editor = editor;
		this.helpsite = helpsite;
		this.timezone = timezone;
		this.cb_gradelevel = cb_gradelevel;
		this.cb_gender = cb_gender;
		this.cb_school = cb_school;
		this.cb_country = cb_country;
		this.cb_province = cb_province;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getMiddlename() {
		return middlename;
	}
	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRegisteripaddr() {
		return registeripaddr;
	}
	public void setRegisteripaddr(String registeripaddr) {
		this.registeripaddr = registeripaddr;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsertype() {
		return usertype;
	}
	public void setUsertype(String usertype) {
		this.usertype = usertype;
	}
	public String getBlock() {
		return block;
	}
	public void setBlock(String block) {
		this.block = block;
	}
	public String getSendmail() {
		return sendmail;
	}
	public void setSendmail(String sendmail) {
		this.sendmail = sendmail;
	}
	public int getAcceptedterms() {
		return acceptedterms;
	}
	public void setAcceptedterms(int acceptedterms) {
		this.acceptedterms = acceptedterms;
	}
	public int getGid() {
		return gid;
	}
	public void setGid(int gid) {
		this.gid = gid;
	}
	public Date getRegisterDate() {
		return registerDate;
	}
	public void setRegisterDate(Date registerDate) {
		this.registerDate = registerDate;
	}
	public String getAdmin_language() {
		return admin_language;
	}
	public void setAdmin_language(String admin_language) {
		this.admin_language = admin_language;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getEditor() {
		return editor;
	}
	public void setEditor(String editor) {
		this.editor = editor;
	}
	public String getHelpsite() {
		return helpsite;
	}
	public void setHelpsite(String helpsite) {
		this.helpsite = helpsite;
	}
	public String getTimezone() {
		return timezone;
	}
	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}
	public String getCb_gradelevel() {
		return cb_gradelevel;
	}
	public void setCb_gradelevel(String cb_gradelevel) {
		this.cb_gradelevel = cb_gradelevel;
	}
	public String getCb_gender() {
		return cb_gender;
	}
	public void setCb_gender(String cb_gender) {
		this.cb_gender = cb_gender;
	}
	public String getCb_school() {
		return cb_school;
	}
	public void setCb_school(String cb_school) {
		this.cb_school = cb_school;
	}
	public String getCb_country() {
		return cb_country;
	}
	public void setCb_country(String cb_country) {
		this.cb_country = cb_country;
	}
	public String getCb_province() {
		return cb_province;
	}
	public void setCb_province(String cb_province) {
		this.cb_province = cb_province;
	}	

}
