package co.edu.unbosque.tstyle.service;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.dto.CheckoutRequest;
import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.user.model.User;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class OrderAuditService {
	private static final Logger LOGGER = LoggerFactory.getLogger(OrderAuditService.class);

	private static final String GPS_SOURCE = "GPS";

	private final AuditService auditService;

	public OrderAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public void orderCreated(User user, OrderResponse order, HttpServletRequest request, CheckoutRequest body) {
		String location = null;
		String source = null;

		if (body != null && body.latitude() != null && body.longitude() != null) {
			location = body.accuracyMeters() == null
					? String.format(Locale.ROOT, "%.6f,%.6f", body.latitude(), body.longitude())
					: String.format(Locale.ROOT, "%.6f,%.6f,%.1f", body.latitude(), body.longitude(),
							body.accuracyMeters());
			source = GPS_SOURCE;
		}

		register(AuditEventType.ORDER_CREATED, user, request, location, source,
				"Pedido " + order.orderNumber());
	}

	public void orderStatusChanged(User admin, OrderResponse order, HttpServletRequest request) {
		register(AuditEventType.ORDER_STATUS_CHANGED, admin, request, null, null,
				"Pedido " + order.orderNumber() + " a " + order.status());
	}

	private void register(AuditEventType type, User user, HttpServletRequest request, String location,
			String source, String detail) {
		try {
			auditService.registerEvent(
					type,
					user == null ? null : user.getId(),
					user == null ? null : user.getEmail(),
					request == null ? null : request.getRemoteAddr(),
					location,
					source,
					detail);
		} catch (RuntimeException e) {
			LOGGER.warn("No fue posible registrar el evento de auditoría {}", type, e);
		}
	}
}
