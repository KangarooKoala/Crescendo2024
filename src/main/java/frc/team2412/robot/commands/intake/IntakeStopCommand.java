package frc.team2412.robot.commands.intake;

import edu.wpi.first.wpilibj2.command.Command;
import frc.team2412.robot.subsystems.IntakeSubsystem;

public class IntakeStopCommand extends Command {
	private final IntakeSubsystem intakeSubsystem;

	public IntakeStopCommand(IntakeSubsystem intakeSubsystem) {
		this.intakeSubsystem = intakeSubsystem;
		addRequirements(intakeSubsystem);
	}

	@Override
	public void initialize() {
		intakeSubsystem.intakeStop();
	}

	@Override
	public boolean isFinished() {
		return false;
	}
}
