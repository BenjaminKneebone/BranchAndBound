package control;

import java.util.ArrayList;
import java.util.Random;

import traindiagrams.TrainDiagramCreator;

import entities.Block;
import entities.BlockExit;
import entities.Engine;
import exceptions.InvalidSpeedException;

import bbentities.BlockOccupation;
import bbentities.Journey;

public class Scheduler {

	private ArrayList<Journey> journeys;
	private ArrayList<Block> blocks;
	private ArrayList<Journey> journeyCopy;
	private ArrayList<Engine> trains;
	double id = 0;

	public Scheduler(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains) {

		this.journeys = journeys;
		this.blocks = blocks;
		this.trains = trains;
		
		// Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
	}
	
	public Scheduler(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains, Journey alteredJourney, int index) {

		System.out.println("---NODE---");

		// Clone blocks
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for (Block b : blocks)
			newBlocks.add(b.clone());

		this.blocks = newBlocks;

		alteredJourney = alteredJourney.clone(this.blocks);
		
		// Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeys = newJournies;
		this.journeys.set(index, alteredJourney);

		// No need to clone trains
		this.trains = trains;
		
		alteredJourney = alteredJourney.clone(this.blocks);

		// Clone journeys, pass in newly cloned blocks
		newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
		journeyCopy.set(index, alteredJourney);
		
		id = Math.random();
	}

	public void schedule() {

		System.out.println("Node Creation");
		for(int x = 0; x < 5; x++)
			blocks.get(x).printBlockDetail();

		
		// Details for earliest block exit for this node
		Block firstArrivalBlock = null;
		double firstArrivalTime = Integer.MAX_VALUE;

		// Schedule next block for each train
		for (Journey j : journeys) {

			// Do not schedule finished journeys
			if (j.isScheduled())
				continue;

			// Do not schedule if train has not started and first block is
			// occupied
			if (j.firstBlock())
				if (j.getNextToBeScheduled().getBlock().isOccupied()) {
					continue;
				}

			if (j.getNextToBeScheduled().getArrSpeed() == 0)
				if (j.getNextToBeScheduled().getBlock().isOccupied()) {
					continue;
				}

			// Get first block to be scheduled on journey
			BlockOccupation firstBlock = j.getNextToBeScheduled();
			
			if (firstBlock.getArrTime() < firstBlock.getBlock()
					.getNextPossibleEntry()) {
				firstBlock.setArrTime(firstBlock.getBlock()
						.getNextPossibleEntry());
				
				if(!j.firstBlock())
					j.getPreviousBlock().setDepTime(firstBlock.getBlock().getNextPossibleEntry());
			}

			Engine train = firstBlock.getTrain();
			BlockExit b = null;

			if (j.lastBlock()) {

				// Last block of the journey
				try {
					// Train coming to a stop at the end of the block
					b = train.exitBlockAtSetSpeed(firstBlock.getBlock(),
							firstBlock.getArrSpeed(), 0);
				} catch (InvalidSpeedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				// Not the last block of the journey

				// Get second block
				BlockOccupation secondBlock = j.getSecondToBeScheduled();

				// If second block has enough space for train to enter at full
				// speed
				if (train.canStopInBlock(secondBlock.getBlock())) {

					// Full speed ahead
					try {
						b = train.timeToTraverse(firstBlock.getBlock(),
								firstBlock.getArrSpeed());
					} catch (InvalidSpeedException e) {
						e.printStackTrace();
					}
				} else {

					// Train must leave block at reduced speed
					int depSpeed = train.highestBlockEntrySpeed(secondBlock
							.getBlock());

					try {
						b = train.exitBlockAtSetSpeed(firstBlock.getBlock(),
								firstBlock.getArrSpeed(), depSpeed);
					} catch (InvalidSpeedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				// If next block is occupied or we arrive before the last train
				// has cleared
				if (secondBlock.getBlock().isOccupied()
						|| ((firstBlock.getArrTime() + b.getTime()) < secondBlock
								.getBlock().getNextPossibleEntry())) {

					// Train needs to stop at the end of the block
					try {
						b = train.exitBlockAtSetSpeed(firstBlock.getBlock(),
								firstBlock.getArrSpeed(), 0);
					} catch (InvalidSpeedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			System.out.println("POSSIBLE OPTION: " + train.getName() + " Block: " + firstBlock.getBlock().getID() + " Arriving at " + firstBlock.getArrTime() + " at " + firstBlock.getArrSpeed() + 
					"Leaving at " + (firstBlock.getArrTime() + b.getTime()) + " at "
					+ b.getSpeed());

			// Time leaving block
			firstBlock.setDepTime(firstBlock.getArrTime() + b.getTime());
			// Speed leaving block
			firstBlock.setDepSpeed(b.getSpeed());

			// If this is the BlockOccupation with the earliest exit
			if (firstBlock.getDepTime() < firstArrivalTime) {
				firstArrivalTime = firstBlock.getDepTime();
				firstArrivalBlock = firstBlock.getBlock();
			}

		}

		createNewNodes(firstArrivalBlock, firstArrivalTime);
	}

	private void createNewNodes(Block firstArrivalBlock, double firstArrival) {
		// ----STEP 3
	
		for (Journey jou : journeys) {
			// Do not schedule finished journeys
			
			if (jou.isScheduled())
				continue;

			// Do not schedule if train has not started and first block is
			// occupied
			if (jou.firstBlock())
				if (jou.getNextToBeScheduled().getBlock().isOccupied()) {
					System.out.println(jou.getTrain().getName() + " "
							+ jou.getNextToBeScheduled().getBlock().getID());
					continue;
				}

			if (jou.getNextToBeScheduled().getArrSpeed() == 0)
				if (jou.getNextToBeScheduled().getBlock().isOccupied()) {
					System.out.println(jou.getTrain().getName() + " "
							+ jou.getNextToBeScheduled().getBlock().getID());
					continue;
				}

			BlockOccupation bo = jou.getNextToBeScheduled();
			BlockOccupation bo2 = null;
			
			Block firstBlockCopy = bo.getBlock().clone();
			Block secondBlockCopy = null;
			
			String y = "hey";
			
			if (!jou.lastBlock()){
				
				bo2 = jou.getSecondToBeScheduled();
				secondBlockCopy = jou.getSecondToBeScheduled().getBlock().clone();
				System.out.println("---bo2: " + bo2);
				y = bo2.toString();
			}

			
			
			// If next block is that of earliest arrival and depij < mav
			if (firstArrivalBlock == bo.getBlock()
					&& bo.getArrTime() <= firstArrival) {
				
				

				
				System.out.println("Updating block "
						+ bo.getBlock().getID() + " for "
						+ bo.getTrain().getName());

				// ----PREVIOUS BLOCK?----//
				// Train halted at the end of the previous block

				if (!jou.firstBlock() && bo.getArrSpeed() == 0) {
					// Unoccupy previous block, update leaving time from
					// block
					jou.getPreviousBlock().getBlock().setOccupied(false);
					jou.getPreviousBlock().getBlock()
							.setNextPossibleEntry(bo.getArrTime());
					firstArrivalBlock.setLastEntry(bo.getArrTime());
				}
				System.out.println("---bo2: " + bo2 + "  " + id);
				// ----LEAVING CURRENT BLOCK?----//
				if (bo.getDepSpeed() != 0) {
					// Update last arrival time for the block, do not occupy
					// this block
					firstArrivalBlock.setNextPossibleEntry(bo.getDepTime());
					firstArrivalBlock.setOccupied(false);
				} else {
					// Occupy this block
					firstArrivalBlock.setOccupied(true);
				}
				System.out.println("---bo2: " + bo2 + "  " + id);
				// ----LAST BLOCK?----//
				if (!jou.lastBlock()) {

					// Not the last block so update entry into next block
					
					System.out.println("Updating next block "
							+ bo2.getBlock().getID());
					bo2.setArrTime(bo.getDepTime());
					bo2.setArrSpeed(bo.getDepSpeed());
					bo2.getBlock().setLastEntry(bo.getDepTime());

					// If we enter the next block, occupy it
					if (bo.getDepSpeed() != 0)
						bo2.getBlock().setOccupied(true);
					
				} else {
					System.out.println("------------------------------------------------------------------------------------------------_SET TO FALSE");
					bo.getBlock().setOccupied(false);
					bo.getBlock().setNextPossibleEntry(bo.getDepTime());
				}
				System.out.println("---bo2: " + bo2 + "  " + id);
				// Move to next block of this journey
				jou.incrementJourney();			

				if (allJourneysScheduled()) {
					for(int x = 0; x < 5; x++)
						blocks.get(x).printBlockDetail();
					System.out.println("-----COMPLETE SCHEDULE-----");

					for (Journey journey : journeys) {
						System.out.println("\n"
								+ journey.getTrain().getName()
								+ " schedule");
						for (BlockOccupation b : journey
								.getBlockOccupations())
							b.printBlockDetail();
					}
					
					TrainDiagramCreator tdc = new TrainDiagramCreator();
					tdc.drawDiagram(journeys, id);

				} else {
					System.out.println("----JUST BEFORE COPY - ALTERATIONS PERFORMED");
					
					for(int x = 0; x < 5; x++)
						blocks.get(x).printBlockDetail();
					
					
					System.out.println("bo2: " + y + "  " + id);
					System.out.println("---CREATING NODE---");
					Scheduler s = new Scheduler(journeyCopy, blocks, trains, jou, jou.getIndex());
					s.schedule();
					
					firstArrivalBlock.copyBlock(firstBlockCopy);
					
					if (!jou.lastBlock()){ 
						System.out.println("bo2: " + y + "  " + id);
						System.out.println("bo2: " + bo2);
						bo2.getBlock().copyBlock(secondBlockCopy);
					}
				}
			}
		}

	}

	private boolean allJourneysScheduled() {

		// Return false if any journey is not fully scheduled
		for (Journey j : journeys)
			if (!j.isScheduled())
				return false;

		return true;
	}

	public void printJourneys() {
		for (Journey j : journeys)
			for (BlockOccupation b : j.getBlockOccupations()) {
				b.printBlockDetail();
			}

		for (Block b : blocks) {
			b.printBlockDetail();
		}
	}

}
