package co.edu.unbosque.tstyle.audit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.tstyle.audit.enums.AuditEventType;
import co.edu.unbosque.tstyle.audit.model.AuditEvent;
import co.edu.unbosque.tstyle.audit.repository.AuditEventRepository;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditEventRepository auditEventRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldRegisterAuditEventWithAllData() {

        auditService.registerEvent(
                AuditEventType.LOGIN_SUCCESS,
                1L,
                "daniel@gmail.com",
                "192.168.1.10",
                "Bogota",
                "IP"
        );

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEvent event = captor.getValue();

        assertEquals(AuditEventType.LOGIN_SUCCESS, event.getEventType());
        assertEquals(1L, event.getUserId());
        assertEquals("daniel@gmail.com", event.getUserEmail());
        assertEquals("192.168.1.10", event.getIpAddress());
        assertEquals("Bogota", event.getLocation());
        assertEquals("IP", event.getLocationSource());
    }

    @Test
    void shouldRegisterAuditEventWithoutUserData() {

        auditService.registerEvent(
                AuditEventType.LOGIN_FAILURE,
                null,
                null,
                "192.168.1.10",
                null,
                null
        );

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);

        verify(auditEventRepository).save(captor.capture());

        AuditEvent event = captor.getValue();

        assertEquals(AuditEventType.LOGIN_FAILURE, event.getEventType());
        assertEquals(null, event.getUserId());
        assertEquals(null, event.getUserEmail());
        assertEquals("192.168.1.10", event.getIpAddress());
        assertEquals(null, event.getLocation());
        assertEquals(null, event.getLocationSource());
    }
}