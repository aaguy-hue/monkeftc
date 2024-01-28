package org.firstinspires.ftc.teamcode;

import static org.firstinspires.ftc.teamcode.util.Encoder.Direction.REVERSE;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.util.Encoder;

@Config
public class Slides {
    // slide constants
    public static double SLIDES_MIN_HEIGHT = 5; // measured in counts
    public static double SLIDES_MAX_HEIGHT = 4000; // to be determined via experimentation

    // pid constants
    public static volatile double Kp = 0.03;
    public static volatile double Ki = 0;
    public static volatile double Kd = 0.0001;

    // idk
    private double targetPosition;
    private ElapsedTime timer = new ElapsedTime();

    // pid temp vars
    private double integralSum;
    private double lastError;
    private double error;

    // instance vars
    private MultipleTelemetry telemetry;
    private DcMotor slideLeft;
    private DcMotor slideRight;
    private Encoder slidesEncoder;

    public Slides(MultipleTelemetry telemetry, DcMotor slideLeft, DcMotor slideRight, Encoder slidesEncoder) {
        this.telemetry     = telemetry;
        this.slideLeft     = slideLeft;
        this.slideRight    = slideRight;
        this.slidesEncoder = slidesEncoder;
        this.slidesEncoder.setDirection(REVERSE);

        _resetTempVars();
    }

    public void updateSlides() {
        /**
         * NOTE: You must run this function each loop iteration. It will move the slides to
         * wherever they should be with PID.
         */
        if (Math.abs(error) >= 10) {
            telemetry.addData("reference", targetPosition);
            telemetry.addData("actual pos", slidesEncoder.getCurrentPosition());
            telemetry.update();

            // obtain the encoder position
            this.slidesEncoder.getCurrentPosition();
            double encoderPosition = slidesEncoder.getCurrentPosition();
            // calculate the error
            error = targetPosition - encoderPosition;

            // rate of change of the error
            double derivative = (error - lastError) / timer.seconds();

            // sum of all error over time
            integralSum = integralSum + (error * timer.seconds());

            double out = (Kp * error) + (Ki * integralSum) + (Kd * derivative);

            _setSlidePower(out);

            lastError = error;

            // reset the timer for next time
            timer.reset();

        }
        else {
            _resetTempVars();
        }
    }

    public void move(double moveAmt) {
        /**
         * Increase/decrease the position of the slides. This is a **relative** move.
         * @param moveAmt - The amount of clicks to increase/decrease the position by.
         */
        this.targetPosition = Math.min(Math.max(this.targetPosition + moveAmt, SLIDES_MIN_HEIGHT), SLIDES_MAX_HEIGHT);
    }

    public void moveUp(double moveAmt) {
        /**
         * Move the slides up by moveAmt clicks. Really just an alias for move(moveAmt).
         * @param moveAmt - The amount of clicks to move the slides up by.
         */
        move(moveAmt);
    }

    public void moveDown(double moveAmt) {
        /**
         * Move the slides down by moveAmt clicks. Really just an alias for move(-moveAmt).
         * @param moveAmt - The amount of clicks to move the slides down by.
         */
        move(-moveAmt);
    }

    public double getTargetPosition() {
        /**
         * Gets the current target position of the slides.
         * @return targetPosition - The target position of the slides.
         */
        return this.targetPosition;
    }

    public void setTargetPosition(double targetPosition) {
        /**
         * Sets the **absolute** position of the slides.
         * @param targetPosition - The new target position for the slides.
         */
        this.targetPosition = targetPosition;
    }

    private void _resetTempVars() {
        this.integralSum = 0;
        this.lastError = 0;
        this.error = 100; // random number
    }

    private void _setSlidePower(double power) {
        this.slideLeft.setPower(-power);
        this.slideRight.setPower(power);
    }

}