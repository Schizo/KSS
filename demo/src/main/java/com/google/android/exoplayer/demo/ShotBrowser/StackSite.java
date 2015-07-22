package com.google.android.exoplayer.demo.ShotBrowser;


/*
 * Data object that holds all of our information about a StackExchange Site.
 */
public class StackSite {

	private String name;
	private String link;
	private String about;
	private String imgUrl;
	private String annotations = "";
	private String id;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getAbout() {
		return about;
	}
	public void setAbout(String about) {
		this.about = about;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getAnnotations() {
		return annotations;
	}
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getId(){return id;}



	@Override
	public String toString() {
		return "StackSite [name=" + name + ", link=" + link + ", about="
				+ about + ", imgUrl=" + imgUrl +  ", annotations=" + annotations + "]";
	}


}
