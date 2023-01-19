// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
// Using "import static an.enum.or.constants.inner.class.*;" helps reduce verbosity
// this replaces "DoubleSolenoid.Value.kForward" with just kForward
// further reading is available at https://www.geeksforgeeks.org/static-import-java/
import static edu.wpi.first.wpilibj.DoubleSolenoid.Value.*;
import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.util.Color;
import frc.robot.Constants.*;

public class Manipulator extends SubsystemBase {
  DoubleSolenoid Intake;  
  public static final int kLedLength = 13;
  public static final int kLedPwmPort = 3;
  // Must be a PWM header, not MXP or DIO
  private final AddressableLED m_led;
  private final AddressableLEDBuffer m_ledBuffer;
  private int m_delay;

  public enum LedMode {
    CUBE,
    CONE,
    KITT,
    OFF,
  };    

  private LedMode m_mode;
  ConSignalLed.gamePiece cube, cone;
  private int m_currentPixel;
  private int m_kittDelta;

  /** Creates a new Manipulator. */
  public Manipulator() {
    Intake = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
    // Set the colors appropriate for each game piece
    cube = new ConSignalLed.gamePiece(50, 0, 100);
    cone = new ConSignalLed.gamePiece(100, 50, 0);

    m_currentPixel = 0;
    m_kittDelta = 1;
    m_delay = 50; // Include a delay during Peridoc() if too processor intensive
    m_led = new AddressableLED(kLedPwmPort);
    m_ledBuffer = new AddressableLEDBuffer(kLedLength);
    // Length is expensive to set, so only set it once, then just update data
    m_led.setLength(m_ledBuffer.getLength());
    
    // Both LED strips MUST Be the same length
    m_mode = LedMode.CUBE;
    m_led.start();
    m_led.setData(m_ledBuffer);
  }

  // These next two methods are temporary just to test peumatics.
  // The actual robot may have TWO separate mechanisms for cone/cube
  // selected by the drive team via OI input TBD
  public void Extend(LedMode cubeOrCone) {
    Intake.set(kForward);
    m_mode = cubeOrCone;
  }

  public void Retract() {
    Intake.set(kReverse);
    m_mode = LedMode.OFF;
  }

  public void setMode(LedMode newMode) {
    m_mode = newMode;
  }

  private void ShowLedPattern() {
    // Note Colors are ACTUALLY in RBG order!!!
    for (var i=0; i<m_ledBuffer.getLength(); i++) {
      switch(m_mode) {
        case CUBE: // Purplish (dark magenta)
          m_ledBuffer.setRGB(i, cube.getRed(), cube.getBlue(), cube.getGreen());
          break;
        case CONE: // Yellow-orange
          m_ledBuffer.setRGB(i, cone.getRed(), cone.getBlue(), cone.getGreen());
          break;
        case KITT: 
          Kitt(); // Cylon Pattern
          break;
        case OFF:
          m_ledBuffer.setRGB(i, 0, 0, 0);
          break;
      } 
      m_led.setData(m_ledBuffer);
    }
  }

  /* Disabled (orange) is too close to cone color  */
  public void Kitt() { 
    for (int i = 0; i < m_ledBuffer.getLength(); i++) {
      Color c = m_ledBuffer.getLED(i);
      int r = (int)Math.round(c.red * 255);
      int g = (int)Math.round(c.green * 255);
      int b = (int)Math.round(c.blue * 255);

      if (r >= 10) r -= 10; else r = 0;
      if (g >= 20) g -= 20; else g = 0;
      if (b >= 20) b -= 20; else b = 0;
      m_ledBuffer.setRGB(i, r, b, g);
    }
    m_ledBuffer.setRGB(m_currentPixel, 64, 64, 64);
    m_led.setData(m_ledBuffer);
    // m_ledB.setData(m_ledBuffer);

    m_currentPixel += m_kittDelta;
    if ((m_currentPixel <= 0) || (m_currentPixel >= kLedLength - 1)) {
      // Ensure valid even when switching modes
      if (m_currentPixel < 0) m_currentPixel = 0;
      if (m_currentPixel > kLedLength - 1) m_currentPixel = kLedLength - 1;
      m_kittDelta = -m_kittDelta;
    }
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    ShowLedPattern();
    if (--m_delay == 0) {
      m_delay = 50;
      System.out.println(m_mode);
    }
  }
}
