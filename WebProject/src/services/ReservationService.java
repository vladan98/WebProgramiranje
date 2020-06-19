package services;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import beans.Apartment;
import beans.Reservation;
import beans.ReservationStatus;
import beans.Role;
import beans.User;
import config.PathConfig;

@Path("/reservation")
public class ReservationService {

	@Context
	ServletContext ctx;

	@Context
	HttpServletRequest request;

	@Path("")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllReservations() throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.ADMIN)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<Reservation> reservations = readReservations();
		return Response.status(Response.Status.OK).entity(reservations).build();
	}

	@Path("/guest")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGuestsReservations() throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.GUEST)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<Reservation> reservations = readReservations();
		reservations.stream().filter(reservation -> reservation.getGuestId().equals(loggedUser.getId()));

		return Response.status(Response.Status.OK).entity(reservations).build();
	}

	@Path("/{id}/guest/cancel")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response cancelReservation(@PathParam("id") String id)
			throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.GUEST)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<Reservation> reservations = readReservations();
		for (Reservation reservation : reservations) {
			if (reservation.getId().equals(id) && reservation.getGuestId().equals(loggedUser.getId())
					&& (reservation.getStatus() == ReservationStatus.ACCEPTED
							|| reservation.getStatus() == ReservationStatus.CREATED)) {
				reservation.setStatus(ReservationStatus.CANCELED);
				writeReservations(reservations);
				return Response.status(Response.Status.OK).entity(reservation).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	
	@Path("/{id}/host")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHostReservations() throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.HOST)
			return Response.status(Response.Status.FORBIDDEN).build();
		
		ArrayList<Reservation> reservations = readReservations();
		reservations.stream().filter(reservation -> {
			try {
				return getApartmentById(reservation.getApartmentId()).getHostId().equals(loggedUser.getId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			
		});
		
		return Response.status(Response.Status.OK).entity(reservations).build();
	}
	
	@Path("/{id}/host/accept")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response acceptReservation(@PathParam("id") String id) throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.HOST)
			return Response.status(Response.Status.FORBIDDEN).build();
		
		ArrayList<Reservation> reservations = readReservations();
		for (Reservation reservation : reservations) {
			if (getApartmentById(reservation.getApartmentId()).getHostId().equals(loggedUser.getId()) && reservation.getStatus() == ReservationStatus.CREATED) {
				reservation.setStatus(ReservationStatus.ACCEPTED);
				writeReservations(reservations);
				return Response.status(Response.Status.OK).entity(reservation).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	
	@Path("/{id}/host/reject")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response rejectReservation(@PathParam("id") String id) throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.HOST)
			return Response.status(Response.Status.FORBIDDEN).build();
		
		ArrayList<Reservation> reservations = readReservations();
		for (Reservation reservation : reservations) {
			if (getApartmentById(reservation.getApartmentId()).getHostId().equals(loggedUser.getId()) && (reservation.getStatus() == ReservationStatus.CREATED || reservation.getStatus() == ReservationStatus.ACCEPTED)) {
				reservation.setStatus(ReservationStatus.REJECTED);
				writeReservations(reservations);
				return Response.status(Response.Status.OK).entity(reservation).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	
	@Path("/{id}/host/end")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response endReservation(@PathParam("id") String id) throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.HOST)
			return Response.status(Response.Status.FORBIDDEN).build();
		
		Calendar c = Calendar.getInstance();
		
		ArrayList<Reservation> reservations = readReservations();
		for (Reservation reservation : reservations) {
			c.setTime(reservation.getStartDate());
			c.add(Calendar.DATE, reservation.getNights());
			Date endDate = c.getTime();
			if (getApartmentById(reservation.getApartmentId()).getHostId().equals(loggedUser.getId()) && endDate.after(new Date()) ) {
				reservation.setStatus(ReservationStatus.ENDED);
				writeReservations(reservations);
				return Response.status(Response.Status.OK).entity(reservation).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	

	private ArrayList<Reservation> readReservations() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File reservationsFile = new File(ctx.getRealPath(".") + PathConfig.RESERVATIONS_FILE);
		boolean created = reservationsFile.createNewFile();
		if (created)
			mapper.writeValue(reservationsFile, new ArrayList<Reservation>());
		return mapper.readValue(reservationsFile, new TypeReference<ArrayList<Reservation>>() {
		});
	}

	private void writeReservations(ArrayList<Reservation> reservations) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File reservationsFile = new File(ctx.getRealPath(".") + PathConfig.RESERVATIONS_FILE);
		reservationsFile.createNewFile();
		mapper.writeValue(reservationsFile, reservations);
	}
	
	private Apartment getApartmentById(String id) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Apartment> apartments = readApartments();
		for (Apartment apartment : apartments) {
			if (apartment.getId().equals(id))
				return apartment;
		}
		return null;
	}

	private ArrayList<Apartment> readApartments() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File apartmentsFile = new File(ctx.getRealPath(".") + PathConfig.APARTMENTS_FILE);
		boolean created = apartmentsFile.createNewFile();
		if (created)
			mapper.writeValue(apartmentsFile, new ArrayList<Apartment>());
		return mapper.readValue(apartmentsFile, new TypeReference<ArrayList<Apartment>>() {
		});
	}
}
