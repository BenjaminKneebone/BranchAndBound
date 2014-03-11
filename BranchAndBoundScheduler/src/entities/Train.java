package entities;

import exceptions.InvalidSpeedException;

public interface Train {

	public String getName();

	public int getID();
	

	public int getLength();

	public int getWeight();

	public int[] getSpeedProfile();

	public void setSpeedProfile(int[] speedProfile);

	/**
	 * @param block
	 * @param speed entry speed
	 * @return BlockExit detailing information if train enters at speed
	 */
	public BlockExit timeToTraverse(BlockOccupation bo, int speed)
			throws InvalidSpeedException;
	
	/**
	 * 
	 * @param block
	 * @param speed
	 * @param finalSpeed
	 * @return BlockExit detailing information if train must leave block at speed
	 * @throws InvalidSpeedException
	 */
	public BlockExit exitBlockAtSetSpeed(BlockOccupation bo, int speed, int finalSpeed)
			throws InvalidSpeedException;
	
	/**
	 * 
	 * @param block
	 * @param speed
	 * @param time
	 * @return BlockExit detailing information if train must take
	 * at least time to traverse block
	 * @throws InvalidSpeedException
	 */
	public BlockExit minimumTimeTraversal(BlockOccupation bo, int speed, double time)
			throws InvalidSpeedException;
	
	/**
	 * @param block
	 * @return True if train can stop in block from full speed
	 */
	public boolean canStopInBlock(BlockOccupation bo);

	/**Return highest speed at which a train can enter and still stop in a block
	 * @param block
	 * @return
	 */
	public int highestBlockEntrySpeed(BlockOccupation bo);

	public void printDetails();
}
