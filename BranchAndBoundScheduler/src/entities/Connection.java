package entities;

public class Connection {

	private Block in;
	private Block out;
	private int length;
	private Join join;
	
	public Connection(Block in, Block out, int length, Join join){
		this.in = in;
		this.out = out;
		this.length = length;
		this.join = join;
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
	
	public Join getJoin(){
		return join;
	}
	
}
