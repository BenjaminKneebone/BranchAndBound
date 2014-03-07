package entities;

public class Connection {

	private Block in;
	private Block out;
	
	public Connection(Block in, Block out){
		this.in = in;
		this.out = out;
	}

	public Block getIn() {
		return in;
	}

	public Block getOut() {
		return out;
	}
	
}
