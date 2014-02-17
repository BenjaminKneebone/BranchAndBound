package control;

import java.util.ArrayList;

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
	private ArrayList<Engine> trains;

	public Scheduler(ArrayList<Journey> journeys, ArrayList<Block> blocks,
			ArrayList<Engine> trains) {

		System.out.println("---NODE---");

		// Clone blocks
		ArrayList<Block> newBlocks = new ArrayList<Block>();
		for (Block b : blocks)
			newBlocks.add(b.clone());

		this.blocks = newBlocks;

		for (Block b : this.blocks)
			b.printBlockDetail();

		// Clone journeys, pass in newly cloned blocks
		ArrayList<Journey> newJournies = new ArrayList<Journey>();
		for (Journey j : journeys)
			newJournies.add(j.clone(this.blocks));

		this.journeys = newJournies;

		// Wipe all arrival times (Last line of step 2)
		for (Journey j : journeys) {
			if (!j.isScheduled() && j.toBeWiped()) {
				j.getNextToBeScheduled().setArrSpeed(0);
				j.getNextToBeScheduled().setArrTime(0);
			} else {
				j.setToBeWiped(true);
			}
		}

		// No need to clone trains
		this.trains = trains;
	}

	public void schedule() {

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
					System.out.println(j.getTrain().getName() + " "
							+ j.getNextToBeScheduled().getBlock().getID());
					continue;
				}

			if (j.getNextToBeScheduled().getArrSpeed() == 0)
				if (j.getNextToBeScheduled().getBlock().isOccupied()) {
					System.out.println(j.getTrain().getName() + " "
							+ j.getNextToBeScheduled().getBlock().getID());
					continue;
				}

			// Get first block to be scheduled on journey
			BlockOccupation firstBlock = j.getNextToBeScheduled();
			System.out.println("Occupied: "
					+ firstBlock.getBlock().isOccupied());

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

				System.out.println("Scheduling last block "
						+ firstBlock.getBlock().getID() + " for "
						+ train.getName());

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

					System.out.println("Scheduling block "
							+ firstBlock.getBlock().getID() + " for "
							+ train.getName() + " at full speed");

					// Full speed ahead
					try {
						b = train.timeToTraverse(firstBlock.getBlock(),
								firstBlock.getArrSpeed());
					} catch (InvalidSpeedException e) {
						e.printStackTrace();
					}
				} else {

					System.out.println("Scheduling block "
							+ firstBlock.getBlock().getID() + " for "
							+ train.getName() + " leaving at reduced speed");

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

					System.out
							.println("Next block still occupied - extend time in block");

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

			System.out.println("Arriving at " + firstBlock.getArrTime()
					+ " at " + firstBlock.getArrSpeed());
			System.out.println("Leaving at "
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

	private void createNewNodes(Block firstArrivalBlock, double firstArrival) {
		// ----STEP 3

		for (Journey jou : journeys) {
			if (!jou.isScheduled()
					&& !(jou.firstBlock() && jou.getNextToBeScheduled()
							.getBlock().isOccupied())) {

				BlockOccupation bo = jou.getNextToBeScheduled();
				BlockOccupation boCopy = bo.clone(blocks);

				BlockOccupation bo2Copy = null;
				if (!jou.lastBlock())
					bo2Copy = jou.getSecondToBeScheduled().clone(blocks);

				// If next block is that of earliest arrival and depij < mav
				if (firstArrivalBlock == bo.getBlock()
						&& bo.getArrTime() <= firstArrival) {

					System.out.println("---CREATING NODE---");
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
						BlockOccupation bo2 = jou.getSecondToBeScheduled();

						System.out.println("Updating next block "
								+ bo2.getBlock().getID());
						bo2.setArrTime(bo.getDepTime());
						bo2.setArrSpeed(bo.getDepSpeed());
						bo2.getBlock().setLastEntry(bo.getDepTime());

						// If we enter the next block, occupy it
						if (bo.getDepSpeed() != 0)
							bo2.getBlock().setOccupied(true);
						
					} else {
						bo.getBlock().setOccupied(false);
					}

					// Move to next block of this journey
					jou.incrementJourney();
					
					// Indicate not to reset arrival details in new node for
					// this journey
					jou.setToBeWiped(false);

					if (allJourneysScheduled()) {
						for (Block b : this.blocks)
							b.printBlockDetail();
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
						tdc.drawDiagram(journeys);

					} else {
						Scheduler s = new Scheduler(journeys, blocks, trains);
						s.schedule();
						
						jou.decrementJourney();

						// replace the altered block occupation with copy made
						// at the beginning
						jou.getBlockOccupations().set(
								boCopy.getBlock().getID(), boCopy);
						
						
						if (!jou.lastBlock()) {
							System.out.println("Resetting block "
									+ bo2Copy.getBlock().getID());

							// replace the altered block occupation with copy made
							// at the beginning
							jou.getBlockOccupations().set(
									bo2Copy.getBlock().getID(), bo2Copy);
						}
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
