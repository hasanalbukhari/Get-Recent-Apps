package com.example.recentapplicationslist;

public class App {

	private String application_name;
	private String application_package_name;
	
	public String getApplication_name() {
		return application_name;
	}
	
	public void setApplication_name(String application_name) {
		this.application_name = application_name;
	}

	public String getApplication_package_name() {
		return application_package_name;
	}

	public void setApplication_package_name(String application_package_name) {
		this.application_package_name = application_package_name;
	}	
	
	@Override
	public boolean equals(Object o) {
		if (o==null)
			return false;
		App p = (App)o;
		return this.application_package_name.equals(p.application_package_name);
	}
	
	@Override
	public int hashCode()
	{
	    final int PRIME = 31;
	    int result = 1;
	    result = PRIME * result + getApplication_package_name().hashCode();
	    return result;
	}
}
