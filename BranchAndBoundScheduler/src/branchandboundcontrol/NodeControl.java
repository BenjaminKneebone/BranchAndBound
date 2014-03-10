package branchandboundcontrol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import traindiagrams.TrainDiagramCreator;
import entities.Block;
import entities.BlockExit;
import entities.BlockOccupation;
import entities.Engine;
import entities.Journey;
import exceptions.InvalidSpeedException;

public class NodeControl {

	//The minimum sum of times found so far
	private double globalMinimum = Integer.MAX_VALUE;
	//The node with the schedule producing the global minimum
	private Node bestNode;
	//Used as a stack to store the nodes
	private Deque<Node> nodes = new ArrayDeque<Node>();
	
	
	public NodeControl(ArrayList<Journey> journeys, ArrayList<Block> blocks, ArrayList<Engine> trains){
		//Root node pushed onto tree
		nodes.push(new Node(journeys, blocks, trains));
		
		//Perform scheduling on node Depth First
		while(!nodes.isEmpty()){
			schedule(nodes.pop());
		}
		
		saveOptimal();
	}

	/*
	* schedule() will attempt to schedule the next BlockOccupation of each
	* Journey. It will alter the BlockOccupation objects stored within
	* journeys. So after schedule has been called, journeys will store the
	* journeys each with another Block scheduled (where feasible). It will find
	* the first arrival time and the block in which this occurs and pass it to
	* the node creation method. Note: No alterations are made to either
	* journeyCopy or blocks
	*/


	public void schedule(Node node) {

		System.out.println("Scheduling " + node.getId());
		
		int x = 0;
		
		// Schedule next block for each train
		for (Journey j : node.getJourneys()) {

			if (!canBeScheduled(j))
				continue;

			x++;
			
			// Get first block to be scheduled on journey
			BlockOccupation currentBlock = j.getNextToBeScheduled();

			//If halted in previous block, push arrival time from that block back
			if (currentBlock.getArrTime() < currentBlock.getBlock().getNextPossibleEntry()) {
				currentBlock.setArrTime(currentBlock.getBlock().getNextPossibleEntry());

				//Push departure time from previous block back
				if (!j.firstBlock())
					j.getPreviousBlock().setDepTime(currentBlock.getBlock().getNextPossibleEntry());
			}

			Engine train = currentBlock.getTrain();
			BlockExit b = null;

			if (j.lastBlock()) {
				/*LAST BLOCK OF JOURNEY, HALT AT END OF BLOCK*/
				
				try {
					b = train.exitBlockAtSetSpeed(currentBlock.getBlock(),
							currentBlock.getArrSpeed(), 0);
				} catch (InvalidSpeedException e) {
					e.printStackTrace();
				}
			} else {
				/*NOT LAST BLOCK OF JOURNEY*/

				BlockOccupation nextBlock = j.getSecondToBeScheduled();

				if(currentBlock.isStation()){
					/*STATION - HALT AT END OF BLOCK*/
					try {
						b = train.exitBlockAtSetSpeed(currentBlock.getBlock(),
								currentBlock.getArrSpeed(), 0);
					} catch (InvalidSpeedException e) {
						e.printStackTrace();
					}
				}else{
				
					if (train.canStopInBlock(nextBlock.getBlock())) {
						/*CAN ENTER NEXT BLOCK AT ANY SPEED - FULL SPEED AHEAD*/
						try {
							b = train.timeToTraverse(currentBlock.getBlock(),
									currentBlock.getArrSpeed());
						} catch (InvalidSpeedException e) {
							e.printStackTrace();
						}
					} else {
						/*MUST ENTER NEXT BLOCK AT REDUCED SPEED*/
						
						int depSpeed = train.highestBlockEntrySpeed(nextBlock.getBlock());
	
						try {
							b = train.exitBlockAtSetSpeed(currentBlock.getBlock(),
									currentBlock.getArrSpeed(), depSpeed);
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	
					
					/*CHECK IF TRAIN CAN ENTER NEXT BLOCK*/
					
					if (nextBlock.getBlock().isOccupied()
							|| ((currentBlock.getArrTime() + b.getTime()) < nextBlock
									.getBlock().getNextPossibleEntry())) {
	
						// Train needs to stop at the end of the block
						try {
							b = train.exitBlockAtSetSpeed(currentBlock.getBlock(),
									currentBlock.getArrSpeed(), 0);
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			currentBlock.setDepTime(currentBlock.getArrTime() + b.getTime());
			currentBlock.setDepSpeed(b.getSpeed());
			currentBlock.setMessage(b.getMessage());

			/*CHECK IF EARLIEST EXIT*/
			if (currentBlock.getDepTime() < node.firstArrivalTime) {
				node.setFirstArrivalTime(currentBlock.getDepTime());
				node.setFirstArrivalBlock(currentBlock.getBlock());
			}

		}
		
		System.out.println("schedule: " + x);
		
		
		createNewNodes(node);
		
	}
	
	/*
	* createNewNodes() will loop through the journeys finding any journey where
	* the the next BlockOccupation's departure time is before the first arrival
	* time and in the same block as the first arrival time.
	*
	* It makes a copy of the block to be altered before any change is made. It
	* then passes journeyCopy (clean list of journeys) and the altered journey
	* into a new node (Where the altered journey is added to a clone of
	* journeyCopy).
	*
	* The altered block is then made to copy the values copied before it was
	* altered (The block is reset to its initial state in this node).
	*/
	
	private void createNewNodes(Node node) {
		// ----STEP 3

		Block firstArrivalBlock = node.getFirstArrivalBlock();
		double firstArrivalTime = node.getFirstArrivalTime();
		
		int childID = 0;

		for (Journey jou : node.getJourneys()) {
			
			if (!canBeScheduled(jou))
				continue;

			BlockOccupation bo = jou.getNextToBeScheduled();
			BlockOccupation bo2 = null;

			// Take copies of affected blocks
			Block firstBlockCopy = bo.getBlock().clone();
			Block secondBlockCopy = null;

			if (!jou.lastBlock()) {
				//Not the last block, take copies of next block
				bo2 = jou.getSecondToBeScheduled();
				secondBlockCopy = jou.getSecondToBeScheduled().getBlock()
						.clone();
			}

			// If next block is that of earliest arrival and depij < mav
			if (firstArrivalBlock == bo.getBlock()
					&& bo.getArrTime() <= firstArrivalTime) {

				// ----WAS HALTED IN PREVIOUS BLOCK?----//

				if (!jou.firstBlock() && bo.getArrSpeed() == 0) {
					// Unoccupy previous block, update leaving time from block
					jou.getPreviousBlock().getBlock().setOccupied(false);
					jou.getPreviousBlock().getBlock()
							.setNextPossibleEntry(bo.getArrTime());
					firstArrivalBlock.setLastEntry(bo.getArrTime());
				}

				// ----LEAVING CURRENT BLOCK?----//
				if (bo.getDepSpeed() != 0) {
					// Update last arrival time for the block, do not occupy this block
					firstArrivalBlock.setNextPossibleEntry(bo.getDepTime());
					firstArrivalBlock.setOccupied(false);
				} else {
					// Occupy this block
					firstArrivalBlock.setOccupied(true);
				}

				// ----LAST BLOCK?----//
				if (!jou.lastBlock()) {
					// Not the last block so update entry into next block

					bo2.setArrTime(bo.getDepTime());
					bo2.setArrSpeed(bo.getDepSpeed());
					bo2.getBlock().setLastEntry(bo.getDepTime());

					// If we enter the next block, occupy it
					if (bo.getDepSpeed() != 0)
						bo2.getBlock().setOccupied(true);

				} else {
					//Last block
					bo.getBlock().setOccupied(false);
					bo.getBlock().setNextPossibleEntry(bo.getDepTime());
				}

				// Move to next block of this journey
				jou.incrementJourney();

				
				/*CREATE NEW NODE OR SCHEDULE COMPLETE*/
				
				if (allJourneysScheduled(node)) {
					scheduleComplete(node);
				} else {
					
					if(getArrivalTimePartialSchedSum(node, jou.getID()) < globalMinimum){
						// Create node and reset blocks
						nodes.push(new Node(node.getJourneyCopy(), node.getBlocks(), node.getTrains(),
								jou, jou.getID(), node.getId().concat(String
										.valueOf(childID++))));
					}
				}
				
				/*RESET BLOCKS*/
				
				firstArrivalBlock.copyBlock(firstBlockCopy);

				if (!jou.lastBlock()) {
					bo2.getBlock().copyBlock(secondBlockCopy);
				}
			}
		}

	}
	
	/**Sums the journeys in this node (Sums the journeys in the clean copy, except
	 * for the ID provided which is taken from the altered version). 
	 * @param node Node to be measured
	 * @param journeyID Altered journey ID
	 * @return Total time sum for the journeys at this node
	 */
	private double getArrivalTimePartialSchedSum(Node node, int journeyID){
		double timeSum = 0;
		
		for(Journey j: node.getJourneyCopy()){
			if(j.getID() != journeyID)
				for(BlockOccupation b: j.getBlockOccupations())
					if(b.getArrTime() != Integer.MAX_VALUE)
						timeSum += b.getArrTime();
		}
		
		for(BlockOccupation b: node.getJourneys().get(journeyID).getBlockOccupations())
			if(b.getArrTime() != Integer.MAX_VALUE)
				timeSum += b.getArrTime();
		
		return timeSum;
	}
	
	private double getArrivalTimeCompleteSchedSum(Node node){
		double timeSum = 0;
		
		for(Journey j: node.getJourneys()){
			for(BlockOccupation b: j.getBlockOccupations())
				if(b.getArrTime() != Integer.MAX_VALUE)
					timeSum += b.getArrTime();
		}
		
		return timeSum;
		
	}
	
	/** 
	 * Takes a complete schedule, calculates if it is the optimal schedule, and stores node in bestNode
	 * if so.  
	 * @param node A leaf node with all journeys completely scheduled
	 */
	private void scheduleComplete(Node node) {
		if(getArrivalTimeCompleteSchedSum(node) < globalMinimum)
			bestNode = node;
	}
	
	/**
	 * Save the optimal schedule found & its train diagram
	 */
	public void saveOptimal(){
		
		
		File sch = new File("schedule/scheduleatnode.txt");

		FileWriter write = null;
		PrintWriter print = null;
		try {
			write = new FileWriter(sch, false);

			print = new PrintWriter(write);

			for (Journey journey : bestNode.getJourneys()) {
				print.write(journey.getTrain().getName() + " schedule\n");

				for (BlockOccupation b : journey.getBlockOccupations())
					print.write(b.getBlockOccupationDetail());
			}
			
			for (Journey journey : bestNode.getJourneys()) {
				print.write(journey.getTrain().getName() + " schedule\n");

				for (BlockOccupation b : journey.getBlockOccupations())
					print.printf(b.getMessage()); 
			
				print.write("\n");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (print != null) {
				print.close();
			}
		}

		TrainDiagramCreator tdc = new TrainDiagramCreator();
		tdc.drawDiagram(bestNode.getJourneys(), String.valueOf(bestNode.getId()));
	}
	

	/**
	 * Checks whether the passed journey can have its next block feasibly scheduled
	 * @param jou
	 * @return false is the journey is completely scheduled, or if waiting to enter a block that is occupied. Otherwise true
	 */
	private boolean canBeScheduled(Journey jou) {

		// Journey already scheduled
		if (jou.isScheduled())
			return false;

		// Train halted before next block and block is occupied
		if (jou.getNextToBeScheduled().getArrSpeed() == 0
				&& jou.getNextToBeScheduled().getBlock().isOccupied())
			return false;

		return true;

	}

	/**
	 * 
	 * @param node 
	 * @return True if all journeys in the node have been scheduled
	 */
	private boolean allJourneysScheduled(Node node) {

		// Return false if any journey is not fully scheduled
		for (Journey j : node.getJourneys())
			if (!j.isScheduled())
				return false;

		return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
