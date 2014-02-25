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

	private double globalMaximum = Integer.MAX_VALUE;
	private Node bestNode;

	Deque<Node> nodes = new ArrayDeque<Node>();
	
	
	public NodeControl(ArrayList<Journey> journeys, ArrayList<Block> blocks, ArrayList<Engine> trains){
		nodes.push(new Node(journeys, blocks, trains));
		
		while(!nodes.isEmpty()){
			schedule(nodes.pop());
		}
		
		saveOptimal();
	}

	public void schedule(Node node) {

		System.out.println("Node Creation");

		// Schedule next block for each train
		for (Journey j : node.getJourneys()) {

			if (!canBeScheduled(j))
				continue;

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
		
		createNewNodes(node);
		
	}
	
	
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
					// Create node and reset blocks
					nodes.push(new Node(node.getJourneyCopy(), node.getBlocks(), node.getTrains(),
							jou, jou.getID(), node.getId().concat(String
									.valueOf(childID++))));
				}
				
				/*RESET BLOCKS*/
				
				firstArrivalBlock.copyBlock(firstBlockCopy);

				if (!jou.lastBlock()) {
					bo2.getBlock().copyBlock(secondBlockCopy);
				}
			}
		}
		
		//nodes.remove(0);

	}
	
	private void scheduleComplete(Node node) {
		
		double timeSum = 0;
		
		for(Journey j: node.getJourneys()){
			for(BlockOccupation b: j.getBlockOccupations())
				if(b.getArrTime() != Integer.MAX_VALUE)
					timeSum += b.getArrTime();
		}
		
		System.out.println("COMPLETE SCHEDULE TIME: " + timeSum);
		
		
		if(timeSum < globalMaximum){
			bestNode = node;
		}
		
	}
	
	public void saveOptimal(){
		
		
		File sch = new File("schedule/scheduleatnode" + bestNode.getId() + ".txt");

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

	private boolean allJourneysScheduled(Node node) {

		// Return false if any journey is not fully scheduled
		for (Journey j : node.getJourneys())
			if (!j.isScheduled())
				return false;

		return true;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
