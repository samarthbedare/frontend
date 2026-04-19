package com.inventory.model;

public class Member {
    private int    number;
    private String name;
    private String role;
    private String workPage;
    private String initials;
    private String photoUrl;
    private String about;
    private java.util.List<String> contributions;
    private String module;
    private java.util.List<String> endpoints;
 
    public Member() {}
 
    public Member(int number, String name, String role,
                  String workPage, String initials, String photoUrl, String about,
                  java.util.List<String> contributions, String module,
                  java.util.List<String> endpoints) {
        this.number   = number;
        this.name     = name;
        this.role     = role;
        this.workPage = workPage;
        this.initials = initials;
        this.photoUrl = photoUrl;
        this.about    = about;
        this.contributions = contributions;
        this.module   = module;
        this.endpoints = endpoints;
    }

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getWorkPage() {
		return workPage;
	}

	public void setWorkPage(String workPage) {
		this.workPage = workPage;
	}

	public String getInitials() {
		return initials;
	}

	public void setInitials(String initials) {
		this.initials = initials;
	}

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public java.util.List<String> getContributions() {
        return contributions;
    }

    public void setContributions(java.util.List<String> contributions) {
        this.contributions = contributions;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public java.util.List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(java.util.List<String> endpoints) {
        this.endpoints = endpoints;
    }
	
	public String getPaddedNumber() {
        return String.format("%02d", number);
    }
}
