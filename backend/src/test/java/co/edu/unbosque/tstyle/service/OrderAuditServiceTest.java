package co.edu.unbosque.tstyle.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.service.AuditService;
import co.edu.unbosque.tstyle.dto.CheckoutRequest;
import co.edu.unbosque.tstyle.dto.OrderResponse;
import co.edu.unbosque.tstyle.enums.EmailStatus;
import co.edu.unbosque.tstyle.enums.OrderStatus;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;

@ExtendWith(MockitoExtension.class)
class OrderAuditServiceTest {
	@Mock
	private AuditService auditService;

	private OrderAuditService orderAuditService;

	private User user;

	private OrderResponse order;

	private MockHttpServletRequest request;

	@BeforeEach
	void setUp() {
		orderAuditService = new OrderAuditService(auditService);
		user = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
		user.setId(1L);
		order = new OrderResponse(1L, "TS-2026-000001", LocalDateTime.of(2026, 11, 15, 10, 30),
				OrderStatus.SHIPPED, 1L, "Ana Pérez", "ana@tstyle.com", List.of(),
				new BigDecimal("100000.00"), EmailStatus.SENT, null, null);
		request = new MockHttpServletRequest();
		request.setRemoteAddr("10.0.0.5");
	}

	@Test
	void orderCreatedStoresGpsLocationWhenCoordinatesArrive() {
		orderAuditService.orderCreated(user, order, request, new CheckoutRequest(4.711, -74.0721, 25.0));

		verify(auditService).registerEvent(AuditEventType.ORDER_CREATED, 1L, "ana@tstyle.com", "10.0.0.5",
				"4.711000,-74.072100,25.0", "GPS", "Pedido TS-2026-000001");
	}

	@Test
	void orderCreatedWithoutCoordinatesStoresOnlyTheIp() {
		orderAuditService.orderCreated(user, order, request, null);

		verify(auditService).registerEvent(AuditEventType.ORDER_CREATED, 1L, "ana@tstyle.com", "10.0.0.5",
				null, null, "Pedido TS-2026-000001");
	}

	@Test
	void orderStatusChangedStoresTheNewStatus() {
		orderAuditService.orderStatusChanged(user, order, request);

		verify(auditService).registerEvent(AuditEventType.ORDER_STATUS_CHANGED, 1L, "ana@tstyle.com",
				"10.0.0.5", null, null, "Pedido TS-2026-000001 a SHIPPED");
	}

	@Test
	void auditFailureDoesNotPropagate() {
		doThrow(new IllegalStateException("db")).when(auditService).registerEvent(any(AuditEventType.class),
				any(), anyString(), anyString(), isNull(), isNull(), anyString());

		assertDoesNotThrow(() -> orderAuditService.orderCreated(user, order, request, null));
	}
}
