
package co.edu.unbosque.tstyle.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Centraliza el envío de correos electrónicos de T Style.
 *
 * Permite enviar códigos para la verificación de cuentas y la recuperación de
 * contraseñas.
 *
 * @author Equipo T Style
 */
@Service
public class EmailCenter {

	private final JavaMailSender mailSender;

	/**
	 * Constructor del servicio de correo.
	 *
	 * @param mailSender componente encargado de enviar correos
	 */
	public EmailCenter(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * Envía un código de verificación al correo del usuario.
	 *
	 * @param email correo del destinatario
	 * @param code  código de verificación
	 */
	public void sendVerificationCode(String email, String code) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(email);
		message.setSubject("Verificación de cuenta - T Style");
		message.setText("Bienvenido a T Style.\n\n" + "Tu código de verificación es: " + code
				+ "\n\nEste código tiene una vigencia de 10 minutos.");

		mailSender.send(message);
	}

	/**
	 * Envía un código de recuperación de contraseña al usuario.
	 *
	 * @param email correo del destinatario
	 * @param code  código de recuperación
	 */
	public void sendPasswordResetCode(String email, String code) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(email);
		message.setSubject("Recuperación de contraseña - T Style");
		message.setText("Hola.\n\n" + "Recibimos una solicitud para recuperar tu contraseña de T Style.\n\n"
				+ "Tu código de recuperación es: " + code + "\n\nEste código tiene una vigencia de 10 minutos.\n\n"
				+ "Si no solicitaste este cambio, puedes ignorar este mensaje.");

		mailSender.send(message);
	}
}
