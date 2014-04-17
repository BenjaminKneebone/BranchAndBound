package entities;

import exceptions.InvalidSpeedException;

public class Engine implements Train{

	private int id;
	private int length; // metres
	private int weight; // kilograms
	private int driForce; // Newtons
	private int braForce; // Newtons
	private double acceleration;// m/s^2
	private double deceleration;// m/s^2
	private String name;
	private int[] speedProfile = new int[10];

	/**
	 * @param id Train ID
	 * @param length in metres
	 * @param weight in kilograms
	 * @param driForce in Newtons
	 * @param breForce in Newtons
	 * @param name 
	 * @param speedProfile A ten int array of speeds at power levels 1-10
	 */
	public Engine(int id, int length, int weight, int driForce, int breForce,
			String name, int[] speedProfile) {
		this.id = id;
		this.length = length;
		this.weight = weight;
		this.driForce = driForce;
		this.braForce = breForce;
		this.name = name;
		this.acceleration = (double) driForce / weight;
		this.deceleration = (double) braForce / weight;
		this.speedProfile = speedProfile;
	}

	public String getName() {
		return name;
	}

	public int getID() {
		return id;
	}

	public int getLength() {
		return length;
	}

	public int getWeight() {
		return weight;
	}

	public int[] getSpeedProfile() {
		return speedProfile;
	}

	public void setSpeedProfile(int[] speedProfile) {
		this.speedProfile = speedProfile;
	}

	/**
	 * Calculates time and speed at block exit assuming train
	 * is at full power across block
	 * 
	 * @param bo
	 *            - block to be traversed
	 * @param speed
	 *            - entry speed (km/h)
	 * @return BlockExit object containing time to traverse block and exit speed
	 *         (km/h)
	 * @throws InvalidSpeedException
	 */
	public BlockOccupation timeToTraverse(BlockOccupation b) throws InvalidSpeedException {

		int blockLength = b.getLength();
		int blockID = b.getBlock().getID();
		int speed = b.getArrSpeed();
				
		
		if (speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);

		double accelerationDist = distanceToFullSpeed(speed);
		double accelerationTime = timeToFullSpeed(speed);
		double time;
		int newVel;
		
		// Not enough time to reach full speed
		if (accelerationDist > blockLength) {

			// The final velocity achievable over the distance (sqrt(u^2 + 2as))
			double finalVel = Math.sqrt(Math.pow(kmhToMs(speed), 2)
					+ (2 * acceleration * blockLength));

			// Round down to nearest km/h
			newVel = (int) Math.floor(msToKmh(finalVel));

			// Convert back to m/s for time calculations
			finalVel = kmhToMs(newVel);

			// The time it will take to reach this velocity (hence time in the
			// block) ((v-u) / a)
			time = (finalVel - kmhToMs(speed)) / acceleration;
			
		} else {

			double distanceAtFullSpeed = blockLength
					- distanceToFullSpeed(speed);

			// Time to accelerate to full speed and to cover remaining distance
			// at full speed
			time = accelerationTime
					+ (distanceAtFullSpeed / kmhToMs(speedProfile[9]));
			
			newVel = speedProfile[9];
		}
		
		/* TIME FOR TRAIN TO ENTER BLOCK */
		
		double speedOutOfPreviousBlock = 0;
		double timeToLeavePreviousBlock = 0;
		
		//If arriving at full speed, trivial to calculate time for train to fully enter block
		if(speed == speedProfile[9]){
			timeToLeavePreviousBlock = length / kmhToMs(speed);
		}else{
			//Train fully enters block whilst accelerating
			if(accelerationDist > length){
				speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * acceleration * length));
				
				timeToLeavePreviousBlock = (length * 2) / (kmhToMs(speed) + speedOutOfPreviousBlock);
			}else{
				//Train reaches full speed before leaving block. Acc time + const time
				timeToLeavePreviousBlock = accelerationTime;
				timeToLeavePreviousBlock += (length - accelerationDist) / 
						kmhToMs(speedProfile[9]);
			}
		}
	
		
		String message = 
				String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Full Power \n", time, blockID, speed, newVel);
		
		b.setDepTime(b.getArrTime() + b.getStationStopTime() + time);
		b.setDepSpeed(newVel);
		b.setMessage(message);
		b.setTimeToEnterBlock(timeToLeavePreviousBlock);
		b.setStationArrivalTime(b.getArrTime() + time);
		
		return b;
	}

	/**
	 * Calculates exit time if train must exit the block
	 * at a certain speed
	 * 
	 * @param bo
	 *            block to be traversed
	 * @param speed
	 *            entry speed (km/h)
	 * @param finalSpeed
	 *            exit speed (km/h)
	 * @return Block Exit item
	 * @throws InvalidSpeedException
	 */
	public BlockOccupation exitBlockAtSetSpeed(BlockOccupation b, int finalSpeed) throws InvalidSpeedException {

		System.out.println("BLOCK LENGTH:" + b.getLength());
		System.out.println("TRAIN LENGTH:" + this.getLength());
		
		int blockLength = b.getLength();
		int blockID = b.getBlock().getID();
		int speed = b.getArrSpeed();
		
		if (speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);

		if (finalSpeed < 0 || finalSpeed > speedProfile[9])
			throw new InvalidSpeedException(finalSpeed, name, speedProfile[9]);

		if (finalSpeed == speed) {
			
			if (speed == 0) {
				// System.out.println("Speed up and down in block");

				int maxSpeed = 1;
				double accelerationTime;
				double accelerationDist;
				double decelerationTime;
				double decelerationDist;
				double constantTime;
				double constantDist;

				for (; maxSpeed < speedProfile[9]; maxSpeed++) {
					// System.out.println("X: " + maxSpeed);
					// System.out.println("Block length" + block.getLength());

					accelerationDist = distanceToChangeSpeed(0, maxSpeed);
					decelerationDist = distanceToChangeSpeed(maxSpeed, 0);

					// System.out.println("Changing speed dist " +
					// (accelerationDist + decelerationDist));

					if (blockLength < accelerationDist + decelerationDist) {
						maxSpeed--;
						break;
					}
				}

				// System.out.println("maxSpeed: " + maxSpeed);

				accelerationTime = timeToChangeSpeed(0, maxSpeed);
				decelerationTime = timeToChangeSpeed(maxSpeed, 0);
				accelerationDist = distanceToChangeSpeed(0, maxSpeed);
				decelerationDist = distanceToChangeSpeed(maxSpeed, 0);
				constantTime = timeToTraverseSetSpeed(blockLength
						- accelerationDist - decelerationDist, maxSpeed);
				constantDist = blockLength
						- accelerationDist - decelerationDist;

				// System.out.println("ACC: " + accelerationTime);
				// System.out.println("DEC: " + decelerationTime);
				// System.out.println("CON: " + constantTime);

				System.out.println("This one");
				
				String message = 
						String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Reaching to %-3dkm/h after %-8.4f, decelerating after %-8.4f  \n", 
								(accelerationTime + constantTime + decelerationTime), blockID, 0, 0, maxSpeed, accelerationTime, (accelerationTime + constantTime));
				
				/* TIME FOR TRAIN TO ENTER BLOCK */
				
				double speedOutOfPreviousBlock = 0;
				double timeToLeavePreviousBlock = 0;
				
				//If train fully enters block whilst accelerating
				if(accelerationDist > length){
					speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * acceleration * length));
					
					timeToLeavePreviousBlock = (length * 2) / (speedOutOfPreviousBlock);
				}else{
					//Train fully enters block whilst constant speed
					if(accelerationDist + constantDist > length){
						//Time to accelerate and traverse rest of distance
						timeToLeavePreviousBlock = accelerationTime;
						timeToLeavePreviousBlock += (length - accelerationDist) / kmhToMs(maxSpeed);
					}else{
						
						System.out.println("HEREEE");
						//Time accelerating, and constant, and whilst slowing down
						timeToLeavePreviousBlock = accelerationTime + constantTime;
						
						speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(maxSpeed), 2) + (2 * (-deceleration) * (length - accelerationDist - constantDist)));
						
						System.out.println(msToKmh(speedOutOfPreviousBlock));
						
						timeToLeavePreviousBlock += (decelerationDist * 2) / (kmhToMs(maxSpeed) + speedOutOfPreviousBlock);
					}
				}
				
				
				
				b.setDepTime(b.getArrTime() + b.getStationStopTime() + accelerationTime + constantTime + decelerationTime);
				b.setDepSpeed(speed);
				b.setMessage(message);
				b.setTimeToEnterBlock(timeToLeavePreviousBlock);
				b.setStationArrivalTime(b.getArrTime() + accelerationTime + constantTime + decelerationTime);
				
				System.out.println(b.getLength());
				
				System.out.println("DEP TIME: " + b.getDepTime());
				
				return b;
			}

			// Stay at constant speed
			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Constant Speed  \n", 
							timeToTraverseSetSpeed(blockLength, speed), blockID, speed, speed);
				
			/* TIME FOR TRAIN TO ENTER BLOCK */
			
			double timeToLeavePreviousBlock = 0;
			
			timeToLeavePreviousBlock = length / kmhToMs(speed);
			
			b.setDepTime(b.getArrTime() + b.getStationStopTime() + timeToTraverseSetSpeed(blockLength,speed));
			b.setDepSpeed(speed);
			b.setMessage(message);
			b.setTimeToEnterBlock(timeToLeavePreviousBlock);
			b.setStationArrivalTime(b.getArrTime() + timeToTraverseSetSpeed(blockLength, speed));
			
			
			return b;
		}

		if (finalSpeed > speed) {
			// Accelerates over the block
			double accelerationTime = timeToChangeSpeed(speed, finalSpeed);
			double accelerationDist = distanceToChangeSpeed(speed, finalSpeed);

			// Time at constant speed
			double constantTime = timeToTraverseSetSpeed(blockLength
					- accelerationDist, finalSpeed);
			
			
			
			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Accelerating after %-8.4f  \n", 
							(accelerationTime + constantTime), blockID, speed, finalSpeed, 0.0);
			
			/* TIME FOR TRAIN TO ENTER BLOCK */
			
			double timeToLeavePreviousBlock = 0;
			double speedOutOfPreviousBlock = 0;
			
			//Train fully enters block whilst accelerating
			if(accelerationDist > length){
				speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * acceleration * length));
				
				timeToLeavePreviousBlock = (length * 2) / (kmhToMs(speed) + speedOutOfPreviousBlock);
			}else{
				//Train reaches full speed before leaving block. Acc time + const time
				timeToLeavePreviousBlock = accelerationTime;
				timeToLeavePreviousBlock += (length - accelerationDist) / 
						kmhToMs(finalSpeed);
			}
			
			b.setDepTime(b.getArrTime() + b.getStationStopTime() + accelerationTime + constantTime);
			b.setDepSpeed(finalSpeed);
			b.setMessage(message);
			b.setTimeToEnterBlock(timeToLeavePreviousBlock);
			b.setStationArrivalTime(b.getArrTime() + accelerationTime + constantTime);
			
			return b;
		} else {
			// Decelerates over the block
			double decelerationTime = timeToChangeSpeed(speed, finalSpeed);
			double decelerationDist = distanceToChangeSpeed(speed, finalSpeed);

			// Time at constant speed
			double constantTime = timeToTraverseSetSpeed(blockLength
					- decelerationDist, speed);
			double constantDist = blockLength - decelerationDist;
			
			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Decelerating after %-8.4f  \n", 
							(constantTime + decelerationTime), blockID, speed, finalSpeed, constantTime);
			
			/* TIME FOR TRAIN TO ENTER BLOCK */
			
			double timeToLeavePreviousBlock = 0;
			double speedOutOfPreviousBlock = 0;
			
			//Train fully enters block whilst accelerating
			if(constantDist > length){
				timeToLeavePreviousBlock += length / 
						kmhToMs(speed);
			}else{
				//Train reaches full speed before leaving block. Acc time + const time
				timeToLeavePreviousBlock = constantTime;
				
				speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * (-deceleration) * (length - constantDist)));
				
				timeToLeavePreviousBlock += ((length - constantDist) * 2) / (kmhToMs(speed) + speedOutOfPreviousBlock);
				
			}
			
			b.setDepTime(b.getArrTime() + b.getStationStopTime() + decelerationTime + constantTime);
			b.setDepSpeed(finalSpeed);
			b.setMessage(message);
			b.setTimeToEnterBlock(timeToLeavePreviousBlock);
			b.setStationArrivalTime(b.getArrTime() + decelerationTime + constantTime);
			
			return b;
		}
	}

	/**
	 * Calculates exit speed if train must spend certain amount
	 * of time in the block
	 * 
	 * @param bo
	 *            - block to be traversed
	 * @param speed
	 *            - entry speed
	 * @param time
	 *            - minimum time to spend in block
	 * @return BlockExit object containing time to traverse block and exit speed
	 * @throws InvalidSpeedException
	 */
	public BlockOccupation minimumTimeTraversal(BlockOccupation b, double time) throws InvalidSpeedException {

		int blockLength = b.getLength();
		int blockID = b.getBlock().getID();
		int speed = b.getArrSpeed();
		
		if (speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);

		int newSpeed = speed;

		// Time taken if speed does not change
		double timeAtInputSpeed = timeToTraverseSetSpeed(blockLength,
				speed);

		// Should train slow down or accelerate in block?
		if (timeAtInputSpeed < time) {

			// Too quick - slow down
			double decelerationTime = 0;
			double decelerationDist = 0;
			double constantTime = 0;

			/*
			 * Iterate down through speeds. When the total time is shorter than
			 * the minimum time that speed is the fastest possible and exiting
			 * the block after minimum time given
			 */

			// Until time taken is longer than minimum time
			while (decelerationTime + constantTime < time) {

				// Decrement speed
				newSpeed--;

				if (newSpeed == 0) {
					// Train must stop at the end of the block
					decelerationTime = timeToStop(speed);
					decelerationDist = distanceToStop(speed);

					// Amount of time to travel at entry speed before
					// decelerating
					constantTime = timeToTraverseSetSpeed(blockLength
							- decelerationDist, speed);
					
					break;
				} else {
					// Time & distance changing speed
					decelerationTime = timeToChangeSpeed(speed, newSpeed);
					decelerationDist = distanceToChangeSpeed(speed, newSpeed);

					// Time at constant speed
					constantTime = timeToTraverseSetSpeed(blockLength
							- decelerationDist, newSpeed);
				}
			}

			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Decelerating after %-8.4f  \n", 
							(decelerationTime + constantTime), blockID, newSpeed, 0, 0.0);

			/* TIME FOR TRAIN TO ENTER BLOCK */
			
			double timeToLeavePreviousBlock = 0;
			double speedOutOfPreviousBlock = 0;
			
			//Train fully enters block whilst decelerating
			if(decelerationDist > length){
				speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * (-deceleration) * length));
				
				timeToLeavePreviousBlock = (length * 2) / (kmhToMs(speed) + speedOutOfPreviousBlock);
			}else{
				//Train reaches full speed before leaving block. Acc time + const time
				timeToLeavePreviousBlock = decelerationTime;
				timeToLeavePreviousBlock += (length - decelerationDist) / 
						kmhToMs(newSpeed);
			}
			
			b.setDepTime(b.getArrTime() + b.getStationStopTime() + decelerationTime + constantTime);
			b.setDepSpeed(newSpeed);
			b.setMessage(message);
			b.setTimeToEnterBlock(timeToLeavePreviousBlock);
			b.setStationArrivalTime(b.getArrTime() + decelerationTime + constantTime);
			
			return b;
		} else {

			// Too slow - can speed up
			double accelerationTime = 100000;
			double accelerationDist = 100000;
			double constantTime = 0;

			/*
			 * Iterate up through speeds. When the total time is shorter than
			 * the minimum time it is too fast so the previous speed tested is
			 * the fastest possible
			 */

			// Until time is shorter than minimum possible
			while (accelerationTime + constantTime > time) {

				// Test new speed
				newSpeed++;

				if (newSpeed == speedProfile[9]) {
					newSpeed ++;
					break;
				} else {
					// Time & distance changing speed
					accelerationTime = timeToChangeSpeed(speed, newSpeed);
					accelerationDist = distanceToChangeSpeed(speed, newSpeed);

					// Time at constant speed
					constantTime = timeToTraverseSetSpeed(blockLength
							- accelerationDist, newSpeed);
				}
			}

			newSpeed--;

			// NewSpeed set to fastest speed possible (Highest speed giving
			// longer time than minimum time)
			accelerationTime = timeToChangeSpeed(speed, newSpeed);
			accelerationDist = distanceToChangeSpeed(speed, newSpeed);
			constantTime = timeToTraverseSetSpeed(blockLength
					- accelerationDist, newSpeed);

			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Accelerating after %-8.4f  \n", 
							(accelerationTime + constantTime), blockID, speed, newSpeed, 0.0);

			/* TIME FOR TRAIN TO ENTER BLOCK */
			
			double timeToLeavePreviousBlock = 0;
			double speedOutOfPreviousBlock = 0;
			
			//Train fully enters block whilst accelerating
			if(accelerationDist > length){
				speedOutOfPreviousBlock = Math.sqrt(Math.pow(kmhToMs(speed), 2) + (2 * acceleration * length));
				
				timeToLeavePreviousBlock = (length * 2) / (kmhToMs(speed) + speedOutOfPreviousBlock);
			}else{
				//Train reaches full speed before leaving block. Acc time + const time
				timeToLeavePreviousBlock = accelerationTime;
				timeToLeavePreviousBlock += (length - accelerationDist) / 
						kmhToMs(newSpeed);
			}
			
			b.setDepTime(b.getArrTime() + b.getStationStopTime() + accelerationTime + constantTime);
			b.setDepSpeed(newSpeed);
			b.setMessage(message);
			b.setTimeToEnterBlock(timeToLeavePreviousBlock);
			b.setStationArrivalTime(b.getArrTime() + accelerationTime + constantTime);
			
			return b;
		}
	}

	/**
	 * Whether a train can stop from full speed in the given block
	 * 
	 * @param block
	 *            block in question
	 * @return true is block is long enough for train to stop at full speed
	 */
	public boolean canStopInBlock(int blockLength) {
		return blockLength > distanceToStop(speedProfile[9]);
	}

	/**
	 * @param block
	 *            block in question
	 * @return The highest speed the train can arrive in the block and stop in
	 *         the block
	 */
	public int highestBlockEntrySpeed(int blockLength) {
		// u = rt(v^2 - 2as), with v = 0
		return (int) Math
				.floor(Math.sqrt(2 * blockLength * deceleration));
	}

	public void printDetails() {
		System.out.println(name + " profile:");
		System.out.println("Length: " + length);
		System.out.println("Weight: " + weight);
		System.out.println("Driving Force: " + driForce);
		System.out.println("Braking Force: " + braForce);
		System.out.println("Acceleration: " + acceleration);
		System.out.println("Deceleration: " + deceleration);
		System.out.print("Speed Profile: ");
		for (int x = 0; x < 10; x++)
			System.out.print(speedProfile[x] + " ");
		System.out.println();
	}

	// ***HELPER FUNCTIONS

	/**
	 * Time taken for train to accelerate/decelerate from initialSpeed to
	 * finalSpeed
	 * 
	 * @param initialSpeed
	 *            initial speed (km/h)
	 * @param finalSpeed
	 *            final speed (km/h)
	 * @return time (seconds)
	 */
	private double timeToChangeSpeed(int initialSpeed, int finalSpeed) {

		if (finalSpeed == initialSpeed)
			return 0;

		if (finalSpeed > initialSpeed)
			return (kmhToMs(finalSpeed) - kmhToMs(initialSpeed)) / acceleration;
		else
			return (kmhToMs(initialSpeed) - kmhToMs(finalSpeed)) / deceleration;
	}

	/**
	 * Distance for train to accelerate/decelerate from initial speed to final
	 * speed
	 * 
	 * @param initialSpeed
	 *            initial speed (km/h)
	 * @param finalSpeed
	 *            final speed (km/h)
	 * @return distance (m)
	 */
	private double distanceToChangeSpeed(int initialSpeed, int finalSpeed) {
		if (finalSpeed == initialSpeed)
			return 0;

		double vsquared;
		double usquared;

		if (finalSpeed > initialSpeed) {

			// Final speed m/s
			vsquared = Math.pow((double) kmhToMs(finalSpeed), 2);

			// Initial speed m/s
			usquared = Math.pow((double) kmhToMs(initialSpeed), 2);

			// Distance in m (v2 - u2 / 2a)
			return (((double) vsquared - usquared) / ((double) 2 * acceleration));

		} else {

			// Final speed m/s
			vsquared = Math.pow((double) kmhToMs(initialSpeed), 2);

			// Initial speed m/s
			usquared = Math.pow((double) kmhToMs(finalSpeed), 2);

			// Distance in m (v2 - u2 / 2a)
			return (((double) vsquared - usquared) / ((double) 2 * deceleration));

		}
	}

	/**
	 * Time to traverse distance at given speed
	 * 
	 * @param distance
	 *            distance to be traversed (m)
	 * @param speed
	 *            speed to traverse at (km/h)
	 * @return time (seconds)
	 */
	private double timeToTraverseSetSpeed(double distance, int speed) {
		return distance / kmhToMs(speed);
	}

	/**
	 * time for train to accelerate to full speed
	 * 
	 * @param speed
	 *            - Speed of train (km/h)
	 * @return time taken to reach full speed (in seconds)
	 */
	private double timeToFullSpeed(int speed) {
		return timeToChangeSpeed(speed, speedProfile[9]);
	}

	/**
	 * distance required for train to reach full speed
	 * 
	 * @param speed
	 *            - Speed of train (km/h)
	 * @return distance needed to reach full speed (in metres)
	 */
	private double distanceToFullSpeed(int speed) {
		return distanceToChangeSpeed(speed, speedProfile[9]);
	}

	/**
	 * @param speed
	 *            - Speed of train (Km/h)
	 * @return time needed to stop train (in metres)
	 */
	private double timeToStop(int speed) {
		return timeToChangeSpeed(speed, 0);
	}

	/**
	 * Train stopping distance
	 * 
	 * @param speed
	 *            - Speed of train
	 * @return distance needed to stop train (in metres)
	 */
	private double distanceToStop(int speed) {
		return distanceToChangeSpeed(speed, 0);
	}

	private double kmhToMs(double kmSpeed) {
		return kmSpeed / 3.6;
	}

	private double msToKmh(double msSpeed) {
		return msSpeed * 3.6;
	}

}
