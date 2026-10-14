package com.nttdata.ms_tickets;

import com.nttdata.ms_tickets.api.TicketsApiDelegate;
import com.nttdata.ms_tickets.model.Ticket;
import com.nttdata.ms_tickets.model.TicketRequest;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketApiDelegateImpl implements TicketsApiDelegate {

    private static final int HORAS_ALTA = 4;
    private static final int HORAS_MEDIA = 8;
    private static final int HORAS_BAJA = 24;

    private final List<Ticket> tickets = new ArrayList<>(List.of(
            ticket(1L, "No carga el reporte mensual",
                    Ticket.EstadoEnum.ABIERTO, Ticket.PrioridadEnum.ALTA, 4),
            ticket(2L, "Error al iniciar sesión",
                    Ticket.EstadoEnum.EN_PROGRESO, Ticket.PrioridadEnum.MEDIA, 2),
            ticket(3L, "Lentitud en la respuesta del sistema",
                    Ticket.EstadoEnum.CERRADO, Ticket.PrioridadEnum.BAJA, 1)
    ));

    private final AtomicLong secuenciaIds = new AtomicLong(
            tickets.stream()
                    .mapToLong(Ticket::getId)
                    .max()
                    .orElse(0L) + 1
    );


    @Override
    public ResponseEntity<List<Ticket>> listarTickets(String estado) {
        List<Ticket> filteredTickets;
        if (estado == null || estado.isEmpty()) {
            filteredTickets = tickets;
        } else {
            filteredTickets = tickets.stream()
                    .filter(ticket -> ticket.getEstado().toString().equalsIgnoreCase(estado))
                    .toList();
        }
        return new ResponseEntity<>(filteredTickets, HttpStatus.OK);

    }

    public ResponseEntity<Ticket> obtenerTicket(Long id) {
        return tickets.stream()
                .filter(ticket -> ticket.getId().equals(id))
                .findFirst()
                .map(ticket -> new ResponseEntity<>(ticket, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<Ticket> crearTicket(TicketRequest request) {
        if (request == null) {
            return ResponseEntity.badRequest().build();
        }
        String titulo = request.getTitulo();
        if (titulo == null || titulo.isBlank()
                || titulo.length() < 5 || titulo.length() > 100
                || request.getPrioridad() == null) {
            return ResponseEntity.badRequest().build();
        }

        int horasEstimadas = switch (request.getPrioridad()) {
            case ALTA -> HORAS_ALTA;
            case MEDIA -> HORAS_MEDIA;
            case BAJA -> HORAS_BAJA;
        };

        // Aquí puedes implementar la lógica para crear un nuevo ticket
        // Por ejemplo, podrías generar un ID único y devolver el ticket creado
        Ticket nuevoTicket = new Ticket()
                .id(secuenciaIds.getAndIncrement())
                .titulo(request.getTitulo())
                .prioridad(Ticket.PrioridadEnum.valueOf(request.getPrioridad().name()))
                .estado(Ticket.EstadoEnum.ABIERTO)
                .horasEstimadas(horasEstimadas);
        tickets.add(nuevoTicket);

        // Aquí deberías agregar el nuevo ticket a tu almacenamiento (base de datos, lista, etc.)
        // Por simplicidad, no se está agregando a la lista en este ejemplo
        return new ResponseEntity<>(nuevoTicket, HttpStatus.CREATED);
    }

    private static Ticket ticket(
            Long id,
            String titulo,
            Ticket.EstadoEnum estado,
            Ticket.PrioridadEnum prioridad,
            Integer horasEstimadas) {
        Ticket t = new Ticket();
        t.setId(id);
        t.setTitulo(titulo);
        t.setEstado(estado);
        t.setPrioridad(prioridad);
        t.setHorasEstimadas(horasEstimadas);
        return t;
    }
}
