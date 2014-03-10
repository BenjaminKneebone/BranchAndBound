package entities;

public class Connection {

	private Block in;
	private Block out;
	private int length;
	
	public Connection(Block in, Block out, int length){
		this.in = in;
		this.out = out;
	}

	public Block getIn() {
		return in;
	}

	public Block getOut() {
		return out;
	}
	
	public int getLength(){
		return length;
	}
	
}
