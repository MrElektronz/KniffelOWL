package de.kniffel.server.commands;

public abstract class ServerCommand {
	protected String prefix;
	protected String[] args;
	
	public ServerCommand(String prefix, String[] args) {
		this.prefix = prefix;
		this.args = args;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String[] getArgs() {
		return args;
	}
	
	
	public abstract void execute();

}
