package de.bfs.dokpool.client.base;

public enum App {
	ELAN,
	DOKSYS,
	RODOS,
	REI;
	
	public static App fromString(String name) {
		return App.valueOf(name.toUpperCase());
	}
}
