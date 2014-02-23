package control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

	private ArrayList<Block> blocks;

	/*
	 * A copy of the journeys passed into this node. All potential alterations
	 * to Journeys are stored in this list (Altered BlockOccupations)
	 */
	private ArrayList<Journey> journeys;
	/*
	 * A copy of the journeys passed into this node. This should not be altered
	 * and is passed into new nodes along with one altered journey
	 */
	private ArrayList<Journey> journeyCopy;
	private ArrayList<Engine> trains;
	private String id;

	// Details for earliest block exit for this node
	Block firstArrivalBlock = null;
	double firstArrivalTime = Integer.MAX_VALUE;

	public Scheduler(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains) {

		this.journeys = journeys;
		this.blocks = blocks;
		this.trains = trains;
		this.id = "0";

		// Clone journeys
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
	}

	private Scheduler(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains, Journey alteredJourney, int index,
			String id) {

		System.out.println("---NODE" + id + "---");

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

		// Add the altered journey that this node was created for
		this.journeys.set(index, alteredJourney);

		// Create another copy of the journeys

		alteredJourney = alteredJourney.clone(this.blocks);

		// Clone journeys, pass in newly cloned blocks
		newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeyCopy = newJournies;
		journeyCopy.set(index, alteredJourney);

		this.id = id;

		// No need to clone trains
		this.trains = trains;
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

	public void schedule() {

		System.out.println("Node Creation");

		// Schedule next block for each train
		for (Journey j : journeys) {

			if (!canBeScheduled(j))
				continue;

			// Get first block to be scheduled on journey
			BlockOccupation firstBlock = j.getNextToBeScheduled();

			if (firstBlock.getArrTime() < firstBlock.getBlock()
					.getNextPossibleEntry()) {
				firstBlock.setArrTime(firstBlock.getBlock()
						.getNextPossibleEntry());

				if (!j.firstBlock())
					j.getPreviousBlock().setDepTime(
							firstBlock.getBlock().getNextPossibleEntry());
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

				if(firstBlock.isStation()){
					try {
						System.out.println("A STATION!!!!");
						b = train.exitBlockAtSetSpeed(firstBlock.getBlock(),
								firstBlock.getArrSpeed(), 0);
						b.addStationTime(120);
					} catch (InvalidSpeedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
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
			}

			System.out.println("POSSIBLE OPTION: " + train.getName()
					+ " Block: " + firstBlock.getBlock().getID()
					+ " Arriving at " + firstBlock.getArrTime() + " at "
					+ firstBlock.getArrSpeed() + "Leaving at "
					+ (firstBlock.getArrTime() + b.getTime()) + " at "
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

	private void createNewNodes(Block firstArrivalBlock, double firstArrival) {
		// ----STEP 3

		int childID = 0;

		for (Journey jou : journeys) {
			// Do not schedule finished journeys

			if (!canBeScheduled(jou))
				continue;

			BlockOccupation bo = jou.getNextToBeScheduled();
			BlockOccupation bo2 = null;

			// Take copies of affected blocks
			Block firstBlockCopy = bo.getBlock().clone();
			Block secondBlockCopy = null;

			if (!jou.lastBlock()) {
				bo2 = jou.getSecondToBeScheduled();
				secondBlockCopy = jou.getSecondToBeScheduled().getBlock()
						.clone();
			}

			// If next block is that of earliest arrival and depij < mav
			if (firstArrivalBlock == bo.getBlock()
					&& bo.getArrTime() <= firstArrival) {

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
					// Update last arrival time for the block, do not occupy
					// this block
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
					bo.getBlock().setOccupied(false);
					bo.getBlock().setNextPossibleEntry(bo.getDepTime());
				}

				// Move to next block of this journey
				jou.incrementJourney();

				if (allJourneysScheduled()) {
					scheduleComplete();
				} else {

					// Create node and reset blocks
					System.out.println("---CREATING NODE "
							+ id.concat(String.valueOf(childID)) + "---");
					Scheduler s = new Scheduler(journeyCopy, blocks, trains,
							jou, jou.getIndex(), id.concat(String
									.valueOf(childID)));
					s.schedule();

					childID++;

					firstArrivalBlock.copyBlock(firstBlockCopy);

					if (!jou.lastBlock()) {
						bo2.getBlock().copyBlock(secondBlockCopy);
					}
				}
			}
		}

	}

	private void scheduleComplete() {
		File sch = new File("schedule/scheduleatnode" + id + ".txt");

		FileWriter write = null;
		PrintWriter print = null;
		try {
			write = new FileWriter(sch, false);

			print = new PrintWriter(write);

			
			for (Journey journey : journeys) {
				print.write(journey.getTrain().getName() + " schedule\n");

				for (BlockOccupation b : journey.getBlockOccupations())
					print.printf("Block %d Arriving %-8.4f (%-3dkm/h) Departing %-8.4f (%-3dkm/h) \n", b.getBlock().getID(), b.getArrTime(), b.getArrSpeed(),  b.getDepTime(), b.getDepSpeed()); 
			
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
		tdc.drawDiagram(journeys, String.valueOf(id));
	}

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
