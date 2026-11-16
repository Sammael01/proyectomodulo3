
package co.edu.unbosque.tstyle.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Centraliza el envio de correos electronicos de T Style.
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
	 * Envia un codigo de verificacion al correo del usuario.
	 *
	 * @param email correo del destinatario
	 * @param code  codigo de verificacion
	 */
	public void sendVerificationCode(String email, String code) {

		SimpleMailMessage message = new SimpleMailMessage();

		message.setTo(email);
		message.setSubject("Verificacion de cuenta - T Style");
		message.setText("Bienvenido a T Style.\n\n" + "Tu codigo de verificacion es: " + code
				+ "\n\nEste codigo tiene una vigencia de 10 minutos.");

		mailSender.send(message);
	}
}
