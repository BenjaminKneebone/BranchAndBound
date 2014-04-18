package branchandboundcontrol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map.Entry;

import traindiagrams.TrainDiagramCreator;
import entities.Block;
import entities.BlockOccupation;
import entities.Engine;
import entities.Journey;
import entities.Train;
import exceptions.InvalidSpeedException;
import filehandling.ScheduleJSONWriter;

public class BBNodeControl {

	//The minimum sum of times found so far
	private double globalMinimum = Integer.MAX_VALUE;
	//The node with the schedule producing the global minimum
	private Node bestNode;
	//Used as a stack to store the nodes
	private Deque<Node> nodes = new ArrayDeque<Node>();
	
	private int cond = 1;
	
	
	//Indicates which blocks are occupied
	private HashMap<Block, ArrayList<Train>> occupied;
	//Indicates the last departure time for each block
	private HashMap<Block, Double> lastEntry;
	//Indicates the next possible entry time for each block
	private HashMap<Block, Double> nextPossibleEntry;
	
	
	public BBNodeControl(ArrayList<Journey> journeys, ArrayList<Block> blocks, ArrayList<Engine> trains){
		//Root node pushed onto tree
		nodes.push(new Node(journeys, blocks, trains));
		
		//Perform scheduling on node Depth First
		while(!nodes.isEmpty()){
			System.out.println("pop");
			schedule(nodes.pop());
			System.out.println("postpop");
		}
		System.out.println("post stack");
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
		
		for(Entry<Block, ArrayList<Train>> p: occupied.entrySet())
			System.out.println("Block: " + p.getKey().getID() + " Num: " + p.getValue().size());
		
		
		
		System.out.println("Best Node: " + bestNode);
		
		lastEntry = node.getLastEntry();
		nextPossibleEntry = node.getNextPossibleEntry();
		
		System.out.println("Scheduling " + node.getId());
		
		System.out.println(node.getJourneys().size());
		
		// Schedule next block for each train
		for (Journey j : node.getJourneys()) {
			
			if (!canBeScheduled(j))
				continue;

			System.out.println("Attemtping to schedule " + j.getTrain().getName() + " in " + j.getNextToBeScheduled().getBlock().getID());
			
			
			
			
			// Get first block to be scheduled on journey
			BlockOccupation currentBlock = j.getNextToBeScheduled();

			if(j.isFirstBlock()){
				currentBlock.setArrTime(nextPossibleEntry.get(currentBlock.getBlock()));
				currentBlock.setStationArrivalTime(currentBlock.getArrTime());
				currentBlock.setDepTime(currentBlock.getArrTime() + currentBlock.getStationStopTime());			
			}else{
			
			
				//If halted in previous block, push arrival time from that block back
				if (currentBlock.getArrTime() < nextPossibleEntry.get(currentBlock.getBlock())) {
					currentBlock.setArrTime(nextPossibleEntry.get(currentBlock.getBlock()));
					j.getPreviousBlock().setDepTime(nextPossibleEntry.get(currentBlock.getBlock()));
					
					/*
					//If previous block was origin, bring station arrival at origin forward so station stop time is maintained.
					if(j.isSecondBlock()){
						BlockOccupation first = j.getPreviousBlock();
						first.setArrTime(first.getDepTime() - first.getStationStopTime());
						first.setStationArrivalTime(first.getArrTime());
					}
					*/
				}
	
				Engine train = currentBlock.getTrain();
			
	
				
				if (j.lastBlock()) {
					/*LAST BLOCK OF JOURNEY, HALT AT END OF BLOCK*/
					
					try {
						currentBlock = train.exitBlockAtSetSpeed(currentBlock, 0);
					} catch (InvalidSpeedException e) {
						e.printStackTrace();
					}
				} else {
					/*NOT LAST BLOCK OF JOURNEY*/
	
					BlockOccupation nextBlock = j.getSecondToBeScheduled();
	
					if(currentBlock.isStation() || occupied.get(nextBlock.getBlock()).size() != 0 || currentBlock.isTurnaround()){
						/*HALT AT END OF BLOCK*/
						try {
							currentBlock = train.exitBlockAtSetSpeed(currentBlock, 0);
						} catch (InvalidSpeedException e) {
							e.printStackTrace();
						}
					}else{
					
						if (train.canStopInBlock(currentBlock.getLength())) {
							/*CAN ENTER NEXT BLOCK AT ANY SPEED - FULL SPEED AHEAD*/
							try {
								currentBlock = train.timeToTraverse(currentBlock);
							} catch (InvalidSpeedException e) {
								e.printStackTrace();
							}
						} else {
							/*MUST ENTER NEXT BLOCK AT REDUCED SPEED*/
							
							int depSpeed = train.highestBlockEntrySpeed(currentBlock.getLength());
		
							try {
								currentBlock = train.exitBlockAtSetSpeed(currentBlock, depSpeed);
							} catch (InvalidSpeedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
		
						
						/*CHECK IF TRAIN ENTERS NEXT BLOCK TOO SOON*/
						if (currentBlock.getDepTime() < nextPossibleEntry.get(nextBlock.getBlock())) {
							// TRAIN MUST SPEND LONGER IN BLOCK
							try {
								currentBlock = train.minimumTimeTraversal(currentBlock, nextPossibleEntry.get(nextBlock.getBlock()) - currentBlock.getArrTime());
							} catch (InvalidSpeedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
			
			System.out.println("---CHECK IF EARLIEST FOUND---");
			
			System.out.println("BLOCK TIME: " + currentBlock.getTimeInBlock() + " SPEED: " + currentBlock.getDepSpeed());
			
			System.out.println("Node First Arrival: " + node.getFirstArrivalTime());
			System.out.println("Jou First Arrival: " + currentBlock.getDepTime());
			System.out.println("Time to leave previous block " + currentBlock.getTimeToEnterBlock());
			
			/*CHECK IF EARLIEST EXIT*/
			if (currentBlock.getDepTime() < node.getFirstArrivalTime()) {
				node.setFirstArrivalTime(currentBlock.getDepTime());
				node.setFirstArrivalBlock(currentBlock.getBlock());
			}
			
			

		}
		
		if(node.getFirstArrivalTime() == Integer.MAX_VALUE){
			System.out.println("No journeys could be scheduled");
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
		System.out.println("Starting creating nodes");
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
				
				BlockOccupation prev = null;
				BlockOccupation current = jou.getNextToBeScheduled();
				BlockOccupation next = null;

				System.out.println("Scheduling " + current.getTrain().getName() + " in " + current.getBlock().getID());
				
				if(!jou.firstBlock()){
					System.out.println("Unoccupy previous blocks");
					prev = jou.getPreviousBlock();
					//Update previous Block now train has left
					nextPossibleEntry.put(prev.getBlock() ,current.getArrTime() + current.getTimeToEnterBlock());
					
					
					//If leaving a bidirectional section, unoccupy those blocks
					if((!current.getBlock().isBidirectional() || jou.lastBlock()) && prev.getBlock().isBidirectional()){
						for(BlockOccupation b: jou.getPreviousBlocks()){
							if(b.getBlock().isBidirectional())
								while(occupied.get(b.getBlock()).remove(jou.getTrain()));
							else
								break;
						}
					}
					
					//Only unoccupy unidirectional blocks
					if(!prev.getBlock().isBidirectional()){
						while(occupied.get(prev.getBlock()).remove(jou.getTrain()));
					}
				
					//Allow previous block to be crossed
					for(Block b: prev.getBlock().getRestrictedBlocks()){
						while(occupied.get(b).remove(jou.getTrain()));
						nextPossibleEntry.put(b ,current.getArrTime() + current.getTimeToEnterBlock());
					}
				}
				
				if(!jou.lastBlock()){
					next = jou.getSecondToBeScheduled();
				}
				
				//Was halted outside this block, update this block with time train arrived
				if(current.getArrSpeed() == 0)
					lastEntry.put(current.getBlock(), current.getArrTime());

				
				// ----LEAVING CURRENT BLOCK?----//
				if (current.getDepSpeed() != 0) {
					//Won't be last Block 
					
					if(!current.getBlock().isBidirectional() && next.getBlock().isBidirectional()){
						occupied.get(currentBlock).add(jou.getTrain());
						
						//occupy all bidirectional blocks in this section
						for(BlockOccupation b: jou.getRemainingBlocks())
							if(b.getBlock().isBidirectional()){
									occupied.get(b.getBlock()).add(jou.getTrain());
									
							}else
								break;
					}else{
					
						
						//Occupy both blocks
						occupied.get(currentBlock).add(jou.getTrain());
						occupied.get(next.getBlock()).add(jou.getTrain());
						
						for(Block b: currentBlock.getRestrictedBlocks()){
							occupied.get(b).add(jou.getTrain());
						}
						
						for(Block b: next.getBlock().getRestrictedBlocks()){
							occupied.get(b).add(jou.getTrain());
						}
					}
					
					
					//Set Arrival time at next Block
					next.setArrTime(current.getDepTime());
					next.setArrSpeed(current.getDepSpeed());
					lastEntry.put(next.getBlock(), current.getArrTime());
				}else{
					
					System.out.println("Exit speed not 0");
					
					//Occupy Block where train is halted
					occupied.get(currentBlock).add(jou.getTrain());
					System.out.println("Occupied: " + currentBlock.getID());
					

					for(Block b: currentBlock.getRestrictedBlocks()){
						occupied.get(b).add(jou.getTrain());
						System.out.println("Occupied Restricted: " + b.getID());
					}
					
					if(jou.firstBlock() && current.getBlock().isBidirectional()){
						//occupy all bidirectional blocks in this section
						for(BlockOccupation b: jou.getRemainingBlocks())
							if(b.getBlock().isBidirectional()){
									occupied.get(b.getBlock()).add(jou.getTrain());
									System.out.println("Occupied Future Block: " + b.getBlock().getID());
							}else
								break;
					}
					
					if(jou.lastBlock()){
						//Last block
						while(occupied.get(currentBlock).remove(jou.getTrain()));
						for(Block b: currentBlock.getRestrictedBlocks()){
							while(occupied.get(b).remove(jou.getTrain()));
							nextPossibleEntry.put(b, current.getDepTime());
						}
						
						nextPossibleEntry.put(current.getBlock(), current.getDepTime());
					}else{
						next.setArrTime(current.getDepTime());
					}
				}

				// Move to next block of this journey
				jou.incrementJourney();

				System.out.println("Pre-node creation");
				for(Entry<Block, ArrayList<Train>> p: occupied.entrySet())
					System.out.println("Block: " + p.getKey().getID() + " Num: " + p.getValue().size());
				
				/*CREATE NEW NODE OR SCHEDULE COMPLETE*/
				
				if (allJourneysScheduled(node)) {
					System.out.println("Look for optimal");
					scheduleComplete(node);
				} else {
					
					if(getArrivalTimePartialSchedSum(node, jou) < globalMinimum){
						// Create node and reset blocks
						nodes.push(new Node(node.getJourneyCopy(), node.getBlocks(), node.getTrains(),
								jou, jou.getID(), node.getId().concat(String
										.valueOf(childID++)), occupied, lastEntry, nextPossibleEntry));
					}
				}
				
				jou.decrementJourney();
				/*RESET BLOCKS*/
				
				if (!jou.lastBlock()) {
					lastEntry.put(next.getBlock(), node.getLastEntryCopy().get(next.getBlock()));
					nextPossibleEntry.put(next.getBlock(), node.getNextPossibleEntryCopy().get(next.getBlock()));
				}
				
				occupied = new HashMap<Block, ArrayList<Train>>();
				
				
				for(Entry<Block, ArrayList<Train>> p: node.getOccupiedCopy().entrySet()){
					ArrayList<Train> trainCopy = new ArrayList<Train>();
					for(Train t : p.getValue())
						trainCopy.add(t);
					
					occupied.put(p.getKey(), trainCopy);
				}
					
				
				if(!jou.firstBlock()){	
					lastEntry.put(prev.getBlock(), node.getLastEntryCopy().get(prev.getBlock()));
					nextPossibleEntry.put(prev.getBlock(), node.getNextPossibleEntryCopy().get(prev.getBlock()));
				}
				
				lastEntry.put(current.getBlock(), node.getLastEntryCopy().get(current.getBlock()));
				nextPossibleEntry.put(current.getBlock(), node.getNextPossibleEntryCopy().get(current.getBlock()));
				
				
			}
		}
		System.out.println("End creating nodes");
		
	}
	
	/**Sums the journeys in this node (Sums the journeys in the clean copy, except
	 * for the ID provided which is taken from the altered version). 
	 * @param node Node to be measured
	 * @param journeyID Altered journey ID
	 * @return Total time sum for the journeys at this node
	 */
	private double getArrivalTimePartialSchedSum(Node node, Journey jou){
		double timeSum = 0;
		
		switch(cond){
		case 1:
		
		//Sum up arrival times of non-altered journeys
		for(Journey j: node.getJourneyCopy()){
			if(j.getID() != jou.getID())
				for(BlockOccupation b: j.getBlockOccupations()){
					if(b.getArrTime() != Integer.MAX_VALUE){
						timeSum += b.getArrTime();
					}
				}
			else
				//Modified journey
				for(BlockOccupation b: jou.getBlockOccupations())
					if(b.getArrTime() != Integer.MAX_VALUE)
						timeSum += b.getArrTime();		
					
		};break;
		
		case 2:
			for(Journey j: node.getJourneyCopy()){
				if(j.getID() != jou.getID()){
					if(!j.firstBlock())
						timeSum += j.getPreviousBlock().getDepTime() - j.getBlockOccupations().get(0).getArrTime();
				}else
					//modified Journey
					timeSum += jou.getPreviousBlock().getDepTime() - jou.getBlockOccupations().get(0).getArrTime();
			};break;	
		}
		return timeSum;
	}
	
	private double getArrivalTimeCompleteSchedSum(Node node){
		double timeSum = 0;
		
		switch(cond){
		case 1:
		
		for(Journey j: node.getJourneys()){
			for(BlockOccupation b: j.getBlockOccupations())
				if(b.getArrTime() != Integer.MAX_VALUE)
					timeSum += b.getArrTime();
		}; break;
		
		case 2:
			for(Journey j: node.getJourneys()){
				ArrayList<BlockOccupation> blocks = j.getBlockOccupations();
				timeSum += (blocks.get(blocks.size() - 1).getDepTime() - blocks.get(0).getArrTime());
			}; break;
			
		}	
		
		return timeSum;
		
	}
	
	/** 
	 * Takes a complete schedule, calculates if it is the optimal schedule, and stores node in bestNode
	 * if so.  
	 * @param node A leaf node with all journeys completely scheduled
	 */
	private void scheduleComplete(Node node) {
		double measure = getArrivalTimeCompleteSchedSum(node);
		System.out.println("Complete Schedule: " + measure);
		
		if(measure < globalMinimum){
			bestNode = node;
			System.out.println(bestNode);
			globalMinimum = measure;
			saveOptimal();
		}
	}
	
	/**
	 * Save the optimal schedule found & its train diagram
	 */
	public void saveOptimal(){
		
		System.out.println(bestNode);
		
		
		System.out.println("Final schedule: " + getArrivalTimeCompleteSchedSum(bestNode));
		
		ScheduleJSONWriter.writeJSONSchedule(bestNode);
		
		File sch = new File("schedule/scheduleatnode.txt");

		FileWriter write = null;
		PrintWriter print = null;
		try {
			write = new FileWriter(sch, false);

			print = new PrintWriter(write);

			for (Journey journey : bestNode.getJourneys()) {
				print.write(journey.getTrain().getName() + " schedule\n");
				
				for (BlockOccupation b : journey.getBlockOccupations()){
					print.write(b.getBlockOccupationDetail());
				}
				
				print.write(" & \\ \n");
			}
			
			for (Journey journey : bestNode.getJourneys()) {
				print.write(journey.getTrain().getName() + " schedule & \\\\ \n");

				print.printf("Departing Station " + journey.getBlockOccupations().get(0).getBlock().getID() + " at " + journey.getBlockOccupations().get(0).getDepTime() + " & \\\\ \n");
				
				for (BlockOccupation b : journey.getBlockOccupations())
					print.printf(b.getMessage()); 
			
				print.write(" & \\ \n");
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
		tdc.drawDiagram(bestNode.getJourneys(), String.valueOf(bestNode.getId()), "BBChart");
	}
	

	/**
	 * Checks whether the passed journey can have its next block feasibly scheduled
	 * @param jou
	 * @return false is the journey is completely scheduled, or if waiting to enter a block that is occupied. Otherwise true
	 */
	private boolean canBeScheduled(Journey jou) {

		// Journey already scheduled
		if (jou.isScheduled()){
			System.out.println("Cannot be scheduled 1");
			return false;
		}

		// Train halted before next block and block is occupied
		if (jou.getNextToBeScheduled().getArrSpeed() == 0 && 
				occupied.get((jou.getNextToBeScheduled().getBlock())).size() != 0){
			if(!(occupied.get((jou.getNextToBeScheduled().getBlock())).size() == 1 && occupied.get((jou.getNextToBeScheduled().getBlock())).get(0) == jou.getTrain())){
				System.out.println("Cannot be scheduled 2");
				return false;
			}
			
			if(jou.getNextToBeScheduled().getBlock().isBidirectional()){
				for(BlockOccupation b: jou.getRemainingBlocks())
					if(b.getBlock().isBidirectional()){
						if(!(occupied.get((jou.getNextToBeScheduled().getBlock())).size() == 1 && occupied.get((jou.getNextToBeScheduled().getBlock())).get(0) == jou.getTrain()))
							return false;
					}else
						break;
				}
			
		}
		
		
		
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
