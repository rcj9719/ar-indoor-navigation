package com.example.rcjoshi.arinphase2;

public class StepDetector {

    private static final int ACCELEROMETER_RING_SIZE = 50;
    private static final int VEL_RING_SIZE = 10;

    // change this threshold according to your sensitivity preferences
    private static final float STEP_THRESHOLD = 50f;

    private static final int STEP_DELAY_NS = 250000000;

    private int accelerometerRingCounter = 0;
    private float[] accelerometerRingX = new float[ACCELEROMETER_RING_SIZE];
    private float[] accelerometerRingY = new float[ACCELEROMETER_RING_SIZE];
    private float[] accelerometerRingZ = new float[ACCELEROMETER_RING_SIZE];
    private int velRingCounter = 0;
    private float[] velRing = new float[VEL_RING_SIZE];
    private long lastStepTimeNs = 0;
    private float oldVelocityEstimate = 0;

    private StepListener listener;

    void registerListener(StepListener listener) {
        this.listener = listener;
    }


    void updateAccelerometer(long timeNs, float x, float y, float z) {
        float[] currentAccelerometer = new float[3];
        currentAccelerometer[0] = x;
        currentAccelerometer[1] = y;
        currentAccelerometer[2] = z;

        // First step is to update our guess of where the global z vector is.
        accelerometerRingCounter++;
        accelerometerRingX[accelerometerRingCounter % ACCELEROMETER_RING_SIZE] = currentAccelerometer[0];
        accelerometerRingY[accelerometerRingCounter % ACCELEROMETER_RING_SIZE] = currentAccelerometer[1];
        accelerometerRingZ[accelerometerRingCounter % ACCELEROMETER_RING_SIZE] = currentAccelerometer[2];

        float[] worldZ = new float[3];
        worldZ[0] = SensorFilter.sum(accelerometerRingX) / Math.min(accelerometerRingCounter, ACCELEROMETER_RING_SIZE);
        worldZ[1] = SensorFilter.sum(accelerometerRingY) / Math.min(accelerometerRingCounter, ACCELEROMETER_RING_SIZE);
        worldZ[2] = SensorFilter.sum(accelerometerRingZ) / Math.min(accelerometerRingCounter, ACCELEROMETER_RING_SIZE);

        float normalization_factor = SensorFilter.norm(worldZ);

        worldZ[0] = worldZ[0] / normalization_factor;
        worldZ[1] = worldZ[1] / normalization_factor;
        worldZ[2] = worldZ[2] / normalization_factor;

        float currentZ = SensorFilter.dot(worldZ, currentAccelerometer) - normalization_factor;
        velRingCounter++;
        velRing[velRingCounter % VEL_RING_SIZE] = currentZ;

        float velocityEstimate = SensorFilter.sum(velRing);

        if (velocityEstimate > STEP_THRESHOLD && oldVelocityEstimate <= STEP_THRESHOLD
                && (timeNs - lastStepTimeNs > STEP_DELAY_NS)) {
            listener.step(timeNs);
            lastStepTimeNs = timeNs;
        }
        oldVelocityEstimate = velocityEstimate;
    }
}