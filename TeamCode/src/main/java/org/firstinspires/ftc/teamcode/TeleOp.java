package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name = "Diff Swerve TeleOp", group = "TeleOp")
public class TeleOp extends OpMode {
    Robot robot;
    private DcMotor EncoderA;
    private DcMotor EncoderB;
    private DcMotor M1;
    private DcMotor R2a;
    private Servo Diff2;
    private Servo Outtake;
    private Servo Plane;
    private Servo Diff1;
    private CRServo IntakeL;
    private DcMotor Lift1;
    private DcMotor Lift2;
    private CRServo IntakeR;

    double Diff1Rest;
    double Diff2Rest;

    //deadband for joysticks
    public double DEADBAND_MAG = 0.1;
    public Vector2d DEADBAND_VEC = new Vector2d(DEADBAND_MAG, DEADBAND_MAG);

    public boolean willResetIMU = true;

    public void init() {
        robot = new Robot(this, false);
        int MaxVelocity;
        int C1;
        int C2;
        int ErrorBand;

        EncoderA = hardwareMap.get(DcMotor.class, "EncoderA");
        EncoderB = hardwareMap.get(DcMotor.class, "EncoderB");
        M1 = hardwareMap.get(DcMotor.class, "M1");
        R2a = hardwareMap.get(DcMotor.class, "R2a");
        Diff2 = hardwareMap.get(Servo.class, "Diff2");
        Outtake = hardwareMap.get(Servo.class, "Outtake");
        Plane = hardwareMap.get(Servo.class, "Plane");
        Diff1 = hardwareMap.get(Servo.class, "Diff1");
        IntakeL = hardwareMap.get(CRServo.class, "IntakeL");
        Lift1 = hardwareMap.get(DcMotor.class, "Lift1");
        Lift2 = hardwareMap.get(DcMotor.class, "Lift2");
        IntakeR = hardwareMap.get(CRServo.class, "IntakeR");
    }

    //allows driver to indicate that the IMU should not be reset
    //used when starting TeleOp after auto or if program crashes in the middle of match
    //relevant because of field-centric controls
    public void init_loop() {
        if (gamepad1.y) {
            willResetIMU = false;
        }
    }
    public void start () {
        if (willResetIMU) robot.initIMU();
        LiftAndDiffSetup();
        M1.setDirection(DcMotor.Direction.REVERSE);
        EncoderA.setDirection(DcMotor.Direction.REVERSE);
        R2a.setDirection(DcMotor.Direction.REVERSE);
        EncoderB.setDirection(DcMotor.Direction.REVERSE);
    }


    public void loop() {
        Vector2d joystick1 = new Vector2d(gamepad1.left_stick_x, -gamepad1.left_stick_y); //LEFT joystick
        Vector2d joystick2 = new Vector2d(gamepad1.right_stick_x, -gamepad2.right_stick_y); //RIGHT joystick

        robot.driveController.updateUsingJoysticks(checkDeadband(joystick1), checkDeadband(joystick2));


        //uncomment for live tuning of ROT_ADVANTAGE constant
        if (gamepad1.b) {
          robot.driveController.moduleRight.ROT_ADVANTAGE += 0.01;
        robot.driveController.moduleLeft.ROT_ADVANTAGE += 0.01;
        }
        if (gamepad1.x) {
          robot.driveController.moduleRight.ROT_ADVANTAGE -= 0.01;
            robot.driveController.moduleLeft.ROT_ADVANTAGE -= 0.01;
        }
        LiftAndDiffLoop();
        Climb();
        telemetry.addData("ROT_ADVANTAGE: ", robot.driveController.moduleLeft.ROT_ADVANTAGE);
        telemetry.addData("TICKS_PER_MODULE_REV: ", robot.driveController.moduleLeft.TICKS_PER_MODULE_REV);


        //to confirm that joysticks are operating properly
        telemetry.addData("Joystick 1", joystick1);
        telemetry.addData("Joystick 2", joystick2);

        telemetry.update();

    }

    //returns zero vector if joystick is within deadband
    public Vector2d checkDeadband(Vector2d joystick) {
        if (Math.abs(joystick.getX()) > DEADBAND_VEC.getX() || Math.abs(joystick.getY()) > DEADBAND_VEC.getY()) {
            return joystick;
        }
        return Vector2d.ZERO;
    }

    private void Climb() {
        if (gamepad2.dpad_down) {
            EncoderA.setPower(-1);
            EncoderB.setPower(1);
        } else if (gamepad2.dpad_up) {
            EncoderA.setPower(1);
            EncoderB.setPower(-1);
        } else {
            EncoderA.setPower(0);
            EncoderB.setPower(0);
        }
    }
    private void LiftAndDiffSetup() {
        Diff2.setDirection(Servo.Direction.REVERSE);
        Outtake.setPosition(0);
        Plane.setPosition(0.5);
        Diff1Rest = 0 + 0.2;
        Diff2Rest = 0.7 - 0.25;
        Diff2.setPosition(Diff2Rest);
        Diff1.setPosition(Diff1Rest);
        IntakeL.setDirection(CRServo.Direction.REVERSE);
        Lift1.setDirection(DcMotor.Direction.REVERSE);
        Lift2.setDirection(DcMotor.Direction.REVERSE);
    }

    /**
     * Describe this function...
     */
    private void LiftAndDiffLoop() {
        boolean IsRightTriggerPressed;
        boolean IsLeftTriggerPressed;

        telemetry.addData("Lift1Pos", Lift1.getCurrentPosition());
        if (gamepad2.right_trigger > 0.1) {
            IsRightTriggerPressed = true;
        } else {
            IsRightTriggerPressed = false;
        }
        if (gamepad2.left_trigger > 0.1) {
            IsLeftTriggerPressed = true;
        } else {
            IsLeftTriggerPressed = false;
        }
        if (Lift1.getCurrentPosition() > -400) {
            if (IsRightTriggerPressed == true) {
                Lift1.setPower(gamepad2.right_trigger * 0.2);
                Lift2.setPower(gamepad2.right_trigger * 0.2);
            } else {
                Lift1.setPower(-1 * gamepad2.left_trigger * 0.4);
                Lift2.setPower(-1 * gamepad2.left_trigger * 0.4);
            }
        } else {
            if (IsRightTriggerPressed == true) {
                Lift1.setPower(gamepad2.right_trigger);
                Lift2.setPower(gamepad2.right_trigger);
            } else {
                Lift1.setPower(-1 * gamepad2.left_trigger);
                Lift2.setPower(-1 * gamepad2.left_trigger);
            }
        }
        if (Lift1.getCurrentPosition() > -70) {
            Diff2.setPosition(Diff2Rest);
            Diff1.setPosition(Diff1Rest);
        } else if (Lift1.getCurrentPosition() < -70 && Lift1.getCurrentPosition() > -350 || IsRightTriggerPressed == true) {
            Diff2.setPosition(Diff2Rest - 0.05);
            Diff1.setPosition(Diff1Rest - 0.05);
        } else {
            if (gamepad2.dpad_right) {
                Diff2.setPosition(Diff2Rest - 0.2);
                Diff1.setPosition(Diff1Rest - 0.41);
            } else {
                Diff2.setPosition(Diff2Rest - (0.7 - 0.15));
                Diff1.setPosition(Diff2Rest - (0.1 - 0.5));
            }
        }
        if (gamepad2.back) {
            Plane.setPosition(0.2);
        } else {
            Plane.setPosition(0.5);
        }
        while (gamepad2.a) {
            Outtake.setPosition(0.25);
        }
        while (gamepad2.b) {
            Outtake.setPosition(0.6);
        }
        if (gamepad2.left_bumper) {
            IntakeL.setPower(1);
            IntakeR.setPower(1);
        } else if (gamepad2.right_bumper) {
            IntakeL.setPower(-1);
            IntakeR.setPower(-1);
        } else {
            IntakeL.setPower(0);
            IntakeR.setPower(0);
        }
    }
}