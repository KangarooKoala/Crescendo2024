package frc.team2412.robot.subsystems;

import com.revrobotics.CANSparkBase;
import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkFlex;
import com.revrobotics.CANSparkLowLevel.MotorType;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkAbsoluteEncoder;
import com.revrobotics.SparkAbsoluteEncoder.Type;
import com.revrobotics.SparkPIDController;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.team2412.robot.Hardware;
import frc.team2412.robot.util.SparkPIDWidget;
import java.util.Map;

public class LauncherSubsystem extends SubsystemBase {
	// CONSTANTS

	// HARDWARE
	private static final int PIVOT_GEARING_RATIO = 180;
	// ANGLE VALUES
	public static final int AMP_AIM_ANGLE = 90;
	public static final int SUBWOOFER_AIM_ANGLE = 54;
	public static final int PODIUM_AIM_ANGLE = 39;
	public static final int TRAP_AIM_ANGLE = 80;
	// MOTOR VALUES
	// max Free Speed: 6784 RPM
	private static final int MAX_FREE_SPEED_RPM = 6784;
	public static final double ANGLE_TOLERANCE = 0.5;
	public static final double RPM_TOLERANCE = 50;
	// RPM
	public static final int SPEAKER_SHOOT_SPEED_RPM = 3392; // 50%
	public static final int TRAP_SHOOT_SPEED_RPM = 2000;
	public static final double ANGLE_MAX_SPEED = 0.3;
	// 3392 RPM = 50% Speed
	// 1356 RPM = 20% Speed
	// 1017 RPM = 15% Speed

	// HARDWARE
	private final CANSparkFlex launcherTopMotor;
	private final CANSparkFlex launcherBottomMotor;
	private final CANSparkFlex launcherAngleOneMotor;
	private final CANSparkFlex launcherAngleTwoMotor;
	private final RelativeEncoder launcherTopEncoder;
	private final RelativeEncoder launcherBottomEncoder;
	private final SparkAbsoluteEncoder launcherAngleEncoder;
	private final SparkPIDController launcherAngleOnePIDController;
	// private final SparkPIDController launcherAngleTwoPIDController;

	private final SparkPIDController launcherTopPIDController;
	private final SparkPIDController launcherBottomPIDController;

	private double rpmSetpoint;
	private double angleSetpoint;

	private final GenericEntry setLauncherSpeedEntry =
			Shuffleboard.getTab("Launcher")
					.addPersistent("Launcher Speed setpoint", SPEAKER_SHOOT_SPEED_RPM)
					.withSize(3, 1)
					.withWidget(BuiltInWidgets.kNumberSlider)
					.withProperties(Map.of("Min", -MAX_FREE_SPEED_RPM, "Max", MAX_FREE_SPEED_RPM))
					.getEntry();

	private final GenericEntry launcherAngleEntry =
			Shuffleboard.getTab("Launcher")
					.add("Launcher angle", 0)
					.withSize(2, 1)
					.withWidget(BuiltInWidgets.kTextView)
					.getEntry();
	private final GenericEntry launcherSpeedEntry =
			Shuffleboard.getTab("Launcher")
					.add("Launcher Speed", 0)
					.withSize(1, 1)
					.withWidget(BuiltInWidgets.kTextView)
					.getEntry();

	private final GenericEntry launcherAngleSpeedEntry =
			Shuffleboard.getTab("Launcher")
					.add("Launcher angle Speed", 0)
					.withSize(2, 1)
					.withWidget(BuiltInWidgets.kTextView)
					.getEntry();

	private final GenericEntry launcherTopFlywheelTemp =
			Shuffleboard.getTab("Launcher")
					.add("top Flywheel temp", 0)
					.withSize(1, 1)
					.withWidget(BuiltInWidgets.kTextView)
					.getEntry();

	private final GenericEntry launcherBottomFlyWheelTemp =
			Shuffleboard.getTab("Launcher")
					.add("bottom Flywheel temp", 0)
					.withSize(1, 1)
					.withWidget(BuiltInWidgets.kTextView)
					.getEntry();

	// Constructor
	public LauncherSubsystem() {

		// MOTOR INSTANCE VARIBLES
		// motors
		launcherTopMotor = new CANSparkFlex(Hardware.LAUNCHER_TOP_MOTOR_ID, MotorType.kBrushless);
		launcherBottomMotor = new CANSparkFlex(Hardware.LAUNCHER_BOTTOM_MOTOR_ID, MotorType.kBrushless);
		launcherAngleOneMotor =
				new CANSparkFlex(Hardware.LAUNCHER_PIVOT_ONE_MOTOR_ID, MotorType.kBrushless);
		launcherAngleTwoMotor =
				new CANSparkFlex(Hardware.LAUNCHER_PIVOT_TWO_MOTOR_ID, MotorType.kBrushless);
		// encoders
		launcherTopEncoder = launcherTopMotor.getEncoder();
		launcherBottomEncoder = launcherBottomMotor.getEncoder();
		launcherAngleEncoder = launcherAngleOneMotor.getAbsoluteEncoder(Type.kDutyCycle);

		// PID controllers
		// Create launcherTopPIDController and launcherTopMotor]
		launcherTopPIDController = launcherTopMotor.getPIDController();
		launcherTopPIDController.setFeedbackDevice(launcherTopEncoder);
		launcherBottomPIDController = launcherBottomMotor.getPIDController();
		launcherBottomPIDController.setFeedbackDevice(launcherBottomEncoder);
		launcherAngleOnePIDController = launcherAngleOneMotor.getPIDController();
		launcherAngleOnePIDController.setFeedbackDevice(launcherAngleEncoder);
		// launcherAngleTwoPIDController = launcherAngleTwoMotor.getPIDController();
		// launcherAngleTwoPIDController.setFeedbackDevice(launcherAngleEncoder);

		Shuffleboard.getTab("Launcher")
				.add(new SparkPIDWidget(launcherAngleOnePIDController, "launcherAngleOnePIDController"));
		// Shuffleboard.getTab("Launcher")
		//		.add(new SparkPIDWidget(launcherAngleTwoPIDController, "launcherAngleTwoPIDController"));
		Shuffleboard.getTab("Launcher")
				.add(new SparkPIDWidget(launcherTopPIDController, "launcherTopPIDController"));
		Shuffleboard.getTab("Launcher")
				.add(new SparkPIDWidget(launcherBottomPIDController, "launcherBottomPIDController"));

		configMotors();
	}

	public void configMotors() {
		launcherTopMotor.restoreFactoryDefaults();
		launcherBottomMotor.restoreFactoryDefaults();
		launcherAngleOneMotor.restoreFactoryDefaults();
		launcherAngleTwoMotor.restoreFactoryDefaults();
		// idle mode (wow)
		launcherTopMotor.setIdleMode(IdleMode.kCoast);
		launcherBottomMotor.setIdleMode(IdleMode.kCoast);
		launcherAngleOneMotor.setIdleMode(IdleMode.kBrake);
		launcherAngleTwoMotor.setIdleMode(IdleMode.kBrake);
		// inveritng the bottom motor lmao
		launcherBottomMotor.setInverted(true);
		launcherAngleTwoMotor.setInverted(true);

		// current limit
		launcherTopMotor.setSmartCurrentLimit(40);
		launcherBottomMotor.setSmartCurrentLimit(40);
		launcherAngleOneMotor.setSmartCurrentLimit(20);
		launcherAngleTwoMotor.setSmartCurrentLimit(20);

		launcherAngleOneMotor.setSoftLimit(CANSparkBase.SoftLimitDirection.kForward, 100);
		launcherAngleOneMotor.setSoftLimit(CANSparkBase.SoftLimitDirection.kReverse, 25);
		launcherAngleTwoMotor.setSoftLimit(CANSparkBase.SoftLimitDirection.kForward, 100);
		launcherAngleTwoMotor.setSoftLimit(CANSparkBase.SoftLimitDirection.kReverse, 25);

		launcherAngleTwoMotor.follow(launcherAngleOneMotor);

		launcherTopMotor.burnFlash();
		launcherBottomMotor.burnFlash();
		launcherAngleOneMotor.burnFlash();
		launcherAngleTwoMotor.burnFlash();

		// PID
		launcherAngleOnePIDController.setP(0.1);
		launcherAngleOnePIDController.setI(0);
		launcherAngleOnePIDController.setD(0);
		launcherAngleOnePIDController.setFF(0);
		launcherAngleOnePIDController.setOutputRange(-ANGLE_MAX_SPEED, ANGLE_MAX_SPEED);

		// launcherAngleTwoPIDController.setP(0.1);
		// launcherAngleTwoPIDController.setI(0);
		// launcherAngleTwoPIDController.setD(0);
		// launcherAngleTwoPIDController.setFF(0);

		launcherTopPIDController.setP(0.1);
		launcherTopPIDController.setI(0);
		launcherTopPIDController.setD(0);
		launcherTopPIDController.setFF(0);

		launcherBottomPIDController.setP(0.1);
		launcherBottomPIDController.setI(0);
		launcherBottomPIDController.setD(0);
		launcherBottomPIDController.setFF(0);

		launcherAngleOneMotor.getEncoder().setPosition(launcherAngleEncoder.getPosition());
		launcherAngleOneMotor.getEncoder().setPositionConversionFactor(PIVOT_GEARING_RATIO);
		launcherAngleTwoMotor.getEncoder().setPosition(launcherAngleEncoder.getPosition());
		launcherAngleTwoMotor.getEncoder().setPositionConversionFactor(PIVOT_GEARING_RATIO);
	}
	// stop launcher motors method
	public void stopLauncher() {
		launcherTopMotor.disable();
		launcherBottomMotor.disable();
	}

	// uses the value from the entry
	public void launch() {
		rpmSetpoint = setLauncherSpeedEntry.getDouble(SPEAKER_SHOOT_SPEED_RPM);
		launcherTopPIDController.setReference(rpmSetpoint, ControlType.kVelocity);
		launcherBottomPIDController.setReference(rpmSetpoint, ControlType.kVelocity);
	}
	// used for presets
	public void launch(double speed) {
		rpmSetpoint = speed;
		launcherTopPIDController.setReference(rpmSetpoint, ControlType.kVelocity);
		launcherBottomPIDController.setReference(rpmSetpoint, ControlType.kVelocity);
	}

	public double getLauncherSpeed() {
		return launcherTopEncoder.getVelocity();
	}
	// returns the degrees of the angle of the launcher
	public double getAngle() {
		// get position returns a double in the form of rotations
		return Units.rotationsToDegrees(launcherAngleEncoder.getPosition());
	}

	public void setAngle(double launcherAngle) {
		angleSetpoint = launcherAngle;
		launcherAngleOnePIDController.setReference(
				Units.degreesToRotations(angleSetpoint), ControlType.kPosition);
		// launcherAngleTwoPIDController.setReference(
		//		Units.degreesToRotations(angleSetpoint), ControlType.kPosition);
	}

	public boolean isAtAngle(double tolerance) {
		return MathUtil.isNear(angleSetpoint, launcherAngleEncoder.getPosition(), tolerance);
	}

	public boolean isAtAngle() {
		return isAtAngle(ANGLE_TOLERANCE);
	}

	public boolean isAtSpeed(double tolerance) {
		return MathUtil.isNear(rpmSetpoint, launcherTopEncoder.getVelocity(), tolerance);
	}

	public boolean isAtSpeed() {
		return isAtSpeed(RPM_TOLERANCE);
	}

	public double getAngleSpeed() {
		return launcherAngleEncoder.getVelocity();
	}

	public void setAngleSpeed(double Speed) {
		// launcherAngleOnePIDController.setReference(Speed, ControlType.kVelocity);
		// launcherAngleTwoPIDController.setReference(Speed, ControlType.kVelocity);
		launcherAngleOneMotor.set(Speed);
	}

	@Override
	public void periodic() {
		launcherAngleEntry.setDouble(getAngle());
		launcherSpeedEntry.setDouble(getLauncherSpeed());
		launcherAngleSpeedEntry.setDouble(getAngleSpeed());
		launcherTopFlywheelTemp.setDouble(launcherTopMotor.getMotorTemperature());
		launcherBottomFlyWheelTemp.setDouble(launcherTopMotor.getMotorTemperature());
	}
}
