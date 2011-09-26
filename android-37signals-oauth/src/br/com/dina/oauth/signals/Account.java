package br.com.dina.oauth.signals;

public class Account {
	private String url;
    private String id;
    private String name;
    private String product;
    
	public Account(String url, String id, String name, String product) {
		this.url = url;
		this.id = id;
		this.name = name;
		this.product = product;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getProduct() {
		return product;
	}
    
}
