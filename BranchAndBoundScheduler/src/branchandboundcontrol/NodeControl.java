package branchandboundcontrol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;

import traindiagrams.TrainDiagramCreator;
import entities.Block;
import entities.BlockExit;
import entities.BlockOccupation;
import entities.Engine;
import entities.Journey;
import exceptions.InvalidSpeedException;
import filehandling.ScheduleJSONWriter;

public class NodeControl {

	//The minimum sum of times found so far
	private double globalMinimum = Integer.MAX_VALUE;
	//The node with the schedule producing the global minimum
	private Node bestNode;
	//Used as a stack to store the nodes
	private Deque<Node> nodes = new ArrayDeque<Node>();
	
	//Indicates which blocks are occupied
	private HashMap<Block, Boolean> occupied;
	//Indicates the last departure time for each block
	private HashMap<Block, Double> lastEntry;
	//Indicates the next possible entry time for each block
	private HashMap<Block, Double> nextPossibleEntry;
	
	
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

		//Retrieve network status at node
		occupied = node.getOccupied();
		lastEntry = node.getLastEntry();
		nextPossibleEntry = node.getNextPossibleEntry();
		
		System.out.println("Scheduling " + node.getId());

		for(Block b: node.getBlocks())
			b.printBlockDetail();
		
		System.out.println(node.getJourneys().size());
		
		// Schedule next block for each train
		for (Journey j : node.getJourneys()) {
			
			if (!canBeScheduled(j))
				continue;

			System.out.println("Attemtping to schedule " + j.getTrain().getName() + " in " + j.getNextToBeScheduled().getBlock().getID());
			
			// Get first block to be scheduled on journey
			BlockOccupation currentBlock = j.getNextToBeScheduled();

			//If halted in previous block, push arrival time from that block back
			if (currentBlock.getArrTime() < nextPossibleEntry.get(currentBlock.getBlock())) {
				currentBlock.setArrTime(nextPossibleEntry.get(currentBlock.getBlock()));
				j.getPreviousBlock().setDepTime(nextPossibleEntry.get(currentBlock.getBlock()));
			}

			Engine train = currentBlock.getTrain();
			BlockExit b = null;
			
			int blockLength = currentBlock.getLength();
			int blockID = currentBlock.getBlock().getID();
			
			if (j.lastBlock()) {
				/*LAST BLOCK OF JOURNEY, HALT AT END OF BLOCK*/
				
				try {
					b = train.exitBlockAtSetSpeed(blockLength, blockID,
							currentBlock.getArrSpeed(), 0);
				} catch (InvalidSpeedException e) {
					e.printStackTrace();
				}
			} else {
				/*NOT LAST BLOCK OF JOURNEY*/

				BlockOccupation nextBlock = j.getSecondToBeScheduled();

				if(currentBlock.isStation() || occupied.get(nextBlock.getBlock())){
					/*HALT AT END OF BLOCK*/
					try {
						b = train.exitBlockAtSetSpeed(blockLength, blockID,
								currentBlock.getArrSpeed(), 0);
					} catch (InvalidSpeedException e) {
						e.printStackTrace();
					}
				}else{
				
					if (train.canStopInBlock(blockLength)) {
						/*CAN ENTER NEXT BLOCK AT ANY SPEED - FULL SPEED AHEAD*/
						try {
							b = train.timeToTraverse(blockLength, blockID,
									currentBlock.getArrSpeed());
						} catch (InvalidSpeedException e) {
							e.printStackTrace();
						}
					} else {
						/*MUST ENTER NEXT BLOCK AT REDUCED SPEED*/
						
						int depSpeed = train.highestBlockEntrySpeed(blockLength);
	
						try {
							b = train.exitBlockAtSetSpeed(blockLength, blockID,
									currentBlock.getArrSpeed(), depSpeed);
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	
					
					/*CHECK IF TRAIN ENTERS NEXT BLOCK TOO SOON*/
					if (currentBlock.getArrTime() + b.getTime() < nextPossibleEntry.get(nextBlock.getBlock())) {
						// TRAIN MUST SPEND LONGER IN BLOCK
						try {
							b = train.minimumTimeTraversal(blockLength, blockID, currentBlock.getArrSpeed(), nextPossibleEntry.get(nextBlock.getBlock()) - currentBlock.getArrTime());
						} catch (InvalidSpeedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			currentBlock.updateWithBlockExit(b);
			
			System.out.println("---CHECK IF EARLIEST FOUND---");
			
			System.out.println("BLOCK TIME: " + b.getTime() + " SPEED: " + b.getSpeed());
			
			System.out.println("Node First Arrival: " + node.getFirstArrivalTime());
			System.out.println("Jou First Arrival: " + currentBlock.getDepTime());
			System.out.println("Time to leave previous block " + currentBlock.getTimeToEnterBlock());
			
			/*CHECK IF EARLIEST EXIT*/
			if (currentBlock.getDepTime() < node.getFirstArrivalTime()) {
				node.setFirstArrivalTime(currentBlock.getDepTime());
				node.setFirstArrivalBlock(currentBlock.getBlock());
			}
			
			

		}
		
		
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
		Block currentBlock = node.getFirstArrivalBlock();
		double firstArrivalTime = node.getFirstArrivalTime();
				
		int childID = 0;

		for (Journey jou : node.getJourneys()) {
			
			if (!canBeScheduled(jou)){
				continue;		
			}
			
			// If next block is that of earliest arrival and depij < mav
			if (currentBlock == jou.getNextToBeScheduled().getBlock()
					&& jou.getNextToBeScheduled().getArrTime() <= firstArrivalTime) {
				
				System.out.println("CREATING NODES");
				
				BlockOccupation prev = jou.getPreviousBlock();
				BlockOccupation current = jou.getNextToBeScheduled();
				BlockOccupation next = null;

				if(!jou.lastBlock()){
					next = jou.getSecondToBeScheduled();
				}
				
				//Update previous Block now train has left
				nextPossibleEntry.put(prev.getBlock() ,current.getArrTime() + current.getTimeToEnterBlock());
				occupied.put(prev.getBlock(), false);
				

				//Was halted outside this block, update this block with time train arrived
				if(current.getArrSpeed() == 0)
					lastEntry.put(current.getBlock(), current.getArrTime());

				
				// ----LEAVING CURRENT BLOCK?----//
				if (current.getDepSpeed() != 0) {
					//Won't be last Block 
					
					//Occupy both blocks
					occupied.put(currentBlock, true);
					occupied.put(next.getBlock(), true);
					
					//Set Arrival time at next Block
					next.setArrTime(current.getDepTime());
					next.setArrSpeed(current.getDepSpeed());
					lastEntry.put(next.getBlock(), current.getArrTime());
				}else{
					//Occupy Block where train is halted
					occupied.put(currentBlock, true);

					if(jou.lastBlock()){
						//Last block
						occupied.put(currentBlock, false);
						nextPossibleEntry.put(current.getBlock(), current.getDepTime());
					}else{
						next.setArrTime(current.getDepTime());
					}
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
										.valueOf(childID++)), occupied, lastEntry, nextPossibleEntry));
					}
				}
				
				jou.decrementJourney();
				/*RESET BLOCKS*/
								
				if (!jou.lastBlock()) {
					occupied.put(next.getBlock(), node.getOccupiedCopy().get(next.getBlock()));
					lastEntry.put(next.getBlock(), node.getLastEntryCopy().get(next.getBlock()));
					nextPossibleEntry.put(next.getBlock(), node.getNextPossibleEntryCopy().get(next.getBlock()));
				}
				
				occupied.put(prev.getBlock(), node.getOccupiedCopy().get(prev.getBlock()));
				occupied.put(current.getBlock(), node.getOccupiedCopy().get(current.getBlock()));
				lastEntry.put(prev.getBlock(), node.getLastEntryCopy().get(prev.getBlock()));
				lastEntry.put(current.getBlock(), node.getLastEntryCopy().get(current.getBlock()));
				nextPossibleEntry.put(prev.getBlock(), node.getNextPossibleEntryCopy().get(prev.getBlock()));
				nextPossibleEntry.put(current.getBlock(), node.getNextPossibleEntryCopy().get(current.getBlock()));
				
				
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
		
		//ScheduleJSONWriter.writeJSONSchedule(bestNode);
		
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

				print.printf("Departing Station " + journey.getBlockOccupations().get(0).getBlock().getID() + " at " + journey.getBlockOccupations().get(0).getDepTime() + "\n");
				
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
				&& occupied.get((jou.getNextToBeScheduled().getBlock())))
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
