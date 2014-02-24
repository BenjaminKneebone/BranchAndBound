package entities;

import exceptions.InvalidSpeedException;

public class Engine implements Train{

	private int id;
	private int length; // mm
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
	 * @param block
	 *            - block to be traversed
	 * @param speed
	 *            - entry speed (km/h)
	 * @return BlockExit object containing time to traverse block and exit speed
	 *         (km/h)
	 * @throws InvalidSpeedException
	 */
	public BlockExit timeToTraverse(Block block, int speed)
			throws InvalidSpeedException {

		if (speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);

		double time;
		int newVel;
		
		// Not enough time to reach full speed
		if (distanceToFullSpeed(speed) > block.getLength()) {

			// The final velocity achievable over the distance (sqrt(u^2 + 2as))
			double finalVel = Math.sqrt(Math.pow(kmhToMs(speed), 2)
					+ (2 * acceleration * block.getLength()));

			// Round down to nearest km/h
			newVel = (int) Math.floor(msToKmh(finalVel));

			// Convert back to m/s for time calculations
			finalVel = kmhToMs(newVel);

			// The time it will take to reach this velocity (hence time in the
			// block) ((v-u) / a)
			time = (finalVel - kmhToMs(speed)) / acceleration;
			
		} else {

			double distanceAtFullSpeed = block.getLength()
					- distanceToFullSpeed(speed);

			// Time to accelerate to full speed and to cover remaining distance
			// at full speed
			time = timeToFullSpeed(speed)
					+ (distanceAtFullSpeed / kmhToMs(speedProfile[9]));
			
			newVel = speedProfile[9];
		}
		
		String message = 
				String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Full Power \n", time, block.getID(), speed, newVel);
		
		return new BlockExit(time, newVel, message);
	}

	/**
	 * Calculates exit time if train must exit the block
	 * at a certain speed
	 * 
	 * @param block
	 *            block to be traversed
	 * @param speed
	 *            entry speed (km/h)
	 * @param finalSpeed
	 *            exit speed (km/h)
	 * @return Block Exit item
	 * @throws InvalidSpeedException
	 */
	public BlockExit exitBlockAtSetSpeed(Block block, int speed, int finalSpeed)
			throws InvalidSpeedException {

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

				for (; maxSpeed < speedProfile[9]; maxSpeed++) {
					// System.out.println("X: " + maxSpeed);
					// System.out.println("Block length" + block.getLength());

					accelerationDist = distanceToChangeSpeed(0, maxSpeed);
					decelerationDist = distanceToChangeSpeed(maxSpeed, 0);

					// System.out.println("Changing speed dist " +
					// (accelerationDist + decelerationDist));

					if (block.getLength() < accelerationDist + decelerationDist) {
						maxSpeed--;
						break;
					}
				}

				// System.out.println("maxSpeed: " + maxSpeed);

				accelerationTime = timeToChangeSpeed(0, maxSpeed);
				decelerationTime = timeToChangeSpeed(maxSpeed, 0);
				accelerationDist = distanceToChangeSpeed(0, maxSpeed);
				decelerationDist = distanceToChangeSpeed(maxSpeed, 0);
				constantTime = timeToTraverseSetSpeed(block.getLength()
						- accelerationDist - decelerationDist, maxSpeed);

				// System.out.println("ACC: " + accelerationTime);
				// System.out.println("DEC: " + decelerationTime);
				// System.out.println("CON: " + constantTime);

				String message = 
						String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Reaching to %-3dkm/h after %-8.4f, decelerating after %-8.4f  \n", 
								(accelerationTime + constantTime + decelerationTime), block.getID(), 0, 0, maxSpeed, accelerationTime, (accelerationTime + constantTime));
				
				return new BlockExit(
						(accelerationTime + constantTime + decelerationTime),
						speed, message);

			}

			// Stay at constant speed
			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Constant Speed  \n", 
							timeToTraverseSetSpeed(block.getLength(), speed), block.getID(), speed, speed);
					
			return new BlockExit(timeToTraverseSetSpeed(block.getLength(),
					speed), speed, message);
		}

		if (finalSpeed > speed) {
			// Accelerates over the block
			double accelerationTime = timeToChangeSpeed(speed, finalSpeed);
			double accelerationDist = distanceToChangeSpeed(speed, finalSpeed);

			// Time at constant speed
			double constantTime = timeToTraverseSetSpeed(block.getLength()
					- accelerationDist, finalSpeed);

			
			
			
			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Accelerating after %-8.4f  \n", 
							(accelerationTime + constantTime), block.getID(), speed, finalSpeed, accelerationTime);

			return new BlockExit(accelerationTime + constantTime, finalSpeed,
					message);
		} else {
			// Decelerates over the block
			double decelerationTime = timeToChangeSpeed(speed, finalSpeed);
			double decelerationDist = distanceToChangeSpeed(speed, finalSpeed);

			// Time at constant speed
			double constantTime = timeToTraverseSetSpeed(block.getLength()
					- decelerationDist, speed);

			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Decelerating after %-8.4f  \n", 
							(constantTime + decelerationTime), block.getID(), speed, finalSpeed, constantTime);
			
			return new BlockExit(decelerationTime + constantTime, finalSpeed,
					message);
		}
	}

	/**
	 * Calculates exit speed if train must spend certain amount
	 * of time in the block
	 * 
	 * @param block
	 *            - block to be traversed
	 * @param speed
	 *            - entry speed
	 * @param time
	 *            - minimum time to spend in block
	 * @return BlockExit object containing time to traverse block and exit speed
	 * @throws InvalidSpeedException
	 */
	public BlockExit minimumTimeTraversal(Block block, int speed, double time)
			throws InvalidSpeedException {

		if (speed < 0 || speed > speedProfile[9])
			throw new InvalidSpeedException(speed, name, speedProfile[9]);

		int newSpeed = speed;

		// Time taken if speed does not change
		double timeAtInputSpeed = timeToTraverseSetSpeed(block.getLength(),
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
					double stopTime = timeToStop(speed);
					double stopDist = distanceToStop(speed);

					// Amount of time to travel at entry speed before
					// decelerating
					constantTime = timeToTraverseSetSpeed(block.getLength()
							- stopDist, speed);

					String message = 
							String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Decelerating after %-8.4f  \n", 
									(stopTime + constantTime), block.getID(), speed, 0, constantTime);


					return new BlockExit(stopTime + constantTime, 0, message);
				} else {
					// Time & distance changing speed
					decelerationTime = timeToChangeSpeed(speed, newSpeed);
					decelerationDist = distanceToChangeSpeed(speed, newSpeed);

					// Time at constant speed
					constantTime = timeToTraverseSetSpeed(block.getLength()
							- decelerationDist, newSpeed);
				}
			}

			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Decelerating after %-8.4f  \n", 
							(decelerationTime + constantTime), block.getID(), newSpeed, 0, constantTime);
					

			return new BlockExit(decelerationTime + constantTime, newSpeed,
					message);
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

				if (newSpeed == 0 || newSpeed == speedProfile[9]) {
					System.out
							.println("Train has come to a halt or reached full speed");
				} else {
					// Time & distance changing speed
					accelerationTime = timeToChangeSpeed(speed, newSpeed);
					accelerationDist = distanceToChangeSpeed(speed, newSpeed);

					// Time at constant speed
					constantTime = timeToTraverseSetSpeed(block.getLength()
							- accelerationDist, newSpeed);
				}
			}

			newSpeed--;

			// NewSpeed set to fastest speed possible (Highest speed giving
			// longer time than minimum time)
			accelerationTime = timeToChangeSpeed(speed, newSpeed);
			accelerationDist = distanceToChangeSpeed(speed, newSpeed);
			constantTime = timeToTraverseSetSpeed(block.getLength()
					- accelerationDist, newSpeed);

			String message = 
					String.format("%-8.4f to traverse %d Entry: %-3dkm/h Exit: %-3dkm/h -- Accelerating after %-8.4f  \n", 
							(accelerationTime + constantTime), block.getID(), speed, newSpeed, constantTime);

			return new BlockExit(accelerationTime + constantTime, newSpeed,
					message);
		}
	}

	/**
	 * Whether a train can stop from full speed in the given block
	 * 
	 * @param block
	 *            block in question
	 * @return true is block is long enough for train to stop at full speed
	 */
	public boolean canStopInBlock(Block block) {
		return block.getLength() > distanceToStop(speedProfile[9]);
	}

	/**
	 * @param block
	 *            block in question
	 * @return The highest speed the train can arrive in the block and stop in
	 *         the block
	 */
	public int highestBlockEntrySpeed(Block block) {
		// u = rt(v^2 - 2as), with v = 0
		return (int) Math
				.floor(Math.sqrt(2 * block.getLength() * deceleration));
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
