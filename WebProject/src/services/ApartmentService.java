package services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.bcel.internal.generic.SWITCH;

import beans.Amenity;
import beans.Apartment;
import beans.ApartmentType;
import beans.Comment;
import beans.Role;
import beans.User;
import config.PathConfig;
import util.apartment.ApartmentDateComparator;
import util.apartment.ApartmentGuestsComparator;
import util.apartment.ApartmentHostComparator;
import util.apartment.ApartmentRoomsComparator;
import util.apartment.ApartmentTypeComparator;
import util.apartment.Order;

@Path("/apartment")
public class ApartmentService {

	@Context
	ServletContext ctx;

	@Context
	HttpServletRequest request;

	@Path("/test")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		return "TESTT";
	}

	@Path("")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllApartments() throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.ADMIN)
			return Response.status(Response.Status.FORBIDDEN).build();
		ArrayList<Apartment> apartments = readApartments();
		return Response.status(Response.Status.OK).entity(apartments).build();
	}

	@Path("/search")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchApartments(@QueryParam("value") String value, @QueryParam("sort") String sort)
			throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Apartment> apartments = readApartments();
		ArrayList<Apartment> searchedApartments = (ArrayList<Apartment>) apartments.stream().filter(apartment -> {
			return apartment.isActive() && (apartment.getLocation().getAddress().getCity().contains(value)
					|| apartment.getLocation().getAddress().getStreet().contains(value));
		}).collect(Collectors.toList());
		switch (sort) {
		case "DATE_ASC":
			searchedApartments.sort(new ApartmentDateComparator(Order.ASC));
			break;

		case "DATE_DESC":
			searchedApartments.sort(new ApartmentDateComparator(Order.DESC));
			break;

		case "GUEST_ASC":
			searchedApartments.sort(new ApartmentGuestsComparator(Order.ASC));
			break;

		case "GUEST_DESC":
			searchedApartments.sort(new ApartmentGuestsComparator(Order.DESC));
			break;

		case "HOST_ASC":
			searchedApartments.sort(new ApartmentHostComparator(Order.ASC));
			break;

		case "HOST_DESC":
			searchedApartments.sort(new ApartmentHostComparator(Order.DESC));
			break;

		case "PRICE_ASC":
			searchedApartments.sort(new ApartmentHostComparator(Order.ASC));
			break;

		case "PRICE_DESC":
			searchedApartments.sort(new ApartmentHostComparator(Order.DESC));
			break;

		case "ROOM_ASC":
			searchedApartments.sort(new ApartmentRoomsComparator(Order.ASC));
			break;

		case "ROOM_DESC":
			searchedApartments.sort(new ApartmentRoomsComparator(Order.DESC));
			break;

		case "TYPE_ASC":
			searchedApartments.sort(new ApartmentTypeComparator(Order.ASC));
			break;

		case "TYPE_DESC":
			searchedApartments.sort(new ApartmentTypeComparator(Order.DESC));
			break;
		}
		return Response.status(Response.Status.OK).entity(searchedApartments).build();
	}

	@Path("/{id}/comments")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComments(@PathParam("id") String id)
			throws JsonParseException, JsonMappingException, IOException {
		Apartment apartment = getApartmentById(id);
		if (apartment == null)
			return Response.status(Response.Status.NOT_FOUND).build();
		ArrayList<Comment> comments = getCommentByApartmentId(id);
		return Response.status(Response.Status.OK).entity(comments).build();
	}

	@Path("/inactive")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getInactiveApartments() throws JsonParseException, JsonMappingException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		ArrayList<Apartment> apartments = readApartments();
		apartments.stream().filter(apartment -> apartment.getHostId().equals(loggedUser.getId()));
		return Response.status(Response.Status.OK).entity(apartments).build();
	}

	@Path("remove/{id}")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeApartment(@PathParam("id") String id)
			throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Apartment> apartments = readApartments();
		for (Apartment a : apartments) {
			if (a.getId().equals(id)) {
				a.setActive(false);
				writeApartments(apartments);
				return Response.status(Response.Status.OK).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@Path("edit/{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response editApartment(@PathParam("id") String id, Apartment apartment)
			throws JsonParseException, JsonMappingException, IOException {

		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() == Role.GUEST)
			return Response.status(Response.Status.FORBIDDEN).build();
		ArrayList<Apartment> apartments = readApartments();
		for (Apartment a : apartments) {
			if (a.getId().equals(id)) {
				a.setApartmentType(apartment.getApartmentType());
				a.setRooms(apartment.getRooms());
				a.setGuests(apartment.getGuests());
				a.setPrice(apartment.getPrice());
				a.setAmenitiesId(apartment.getAmenitiesId());
				writeApartments(apartments);
				return Response.status(Response.Status.OK).build();
			}
		}

		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@Path("/filter")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response filterApartments(@QueryParam("sort") String sort, Apartment apartment)
			throws JsonParseException, JsonMappingException, IOException {

		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() == Role.GUEST)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<Apartment> apartments = readApartments();
		if (apartment.getApartmentType() != null)
			apartments.stream().filter(a -> a.getApartmentType() == apartment.getApartmentType());
		if (apartment.getRooms() != 0)
			apartments.stream().filter(a -> a.getRooms() == apartment.getRooms());
		if (apartment.getGuests() != 0)
			apartments.stream().filter(a -> a.getGuests() == apartment.getGuests());
		if (apartment.getAmenitiesId() != null)
			apartment.getAmenitiesId().stream()
					.filter(a -> apartment.getAmenitiesId().stream().anyMatch(a2 -> a2.equals(a)));
		return Response.status(Response.Status.OK).entity(apartments).build();
	}

	@Path("")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response addApartment(Apartment a) throws IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.HOST)
			return Response.status(Response.Status.FORBIDDEN).build();
		Apartment apartment = new Apartment(a.getApartmentType(), a.getRooms(), a.getGuests(), a.getLocation(),
				a.getDates(), a.getHostId(), new ArrayList<String>(), new ArrayList<String>(), a.getPrice(),
				a.getCheckIn(), a.getChekOut(), a.getAmenitiesId(), new ArrayList<String>(), false);
		ArrayList<Apartment> apartments = readApartments();
		apartments.add(apartment);
		writeApartments(apartments);
		return Response.status(Response.Status.OK).entity(apartment).build();
	}

	@Path("/{id}/image")
	@POST
	public Response uploadImage(InputStream in, @HeaderParam("Content-Type") String fileType,
			@HeaderParam("Content-Length") long fileSize, @PathParam("id") String apartmentId) throws IOException {
		String fileName = UUID.randomUUID().toString();
		if (fileType.equals("image/jpeg")) {
			fileName += ".jpg";
		} else {
			fileName += ".png";
		}
		System.out.println(apartmentId);
		java.nio.file.Path BASE_DIR = Paths.get(ctx.getRealPath(".") + PathConfig.APARTMENT_IMAGES);
		Files.copy(in, BASE_DIR.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
		
		ArrayList<Apartment> apartments = readApartments();
		for (Apartment apartment : apartments) {
			if (apartment.getId().equals(apartmentId))
				apartment.getImages().add(fileName);
				writeApartments(apartments);
				return Response.status(Response.Status.OK).entity(in).build();
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}
	@Path("/{id}/image")
	@GET
	public Response getImage(@PathParam("id") String id) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<FileInputStream> fileInputStreams = new ArrayList<FileInputStream>();
		Apartment apartment = getApartmentById(id);
		if (apartment == null) 
			return Response.status(Response.Status.NOT_FOUND).build();
		for (String image : apartment.getImages()) {
			FileInputStream fileInputStream = new FileInputStream(new File(ctx.getRealPath(".") + PathConfig.APARTMENT_IMAGES + File.separator + image));
			fileInputStreams.add(fileInputStream);
		}
		return Response.status(Response.Status.OK).entity(fileInputStreams).build();
		
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

	private void writeApartments(ArrayList<Apartment> apartments) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File apartmentsFile = new File(ctx.getRealPath(".") + PathConfig.APARTMENTS_FILE);
		apartmentsFile.createNewFile();
		mapper.writeValue(apartmentsFile, apartments);
	}

	private ArrayList<Comment> readComments() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File commentsFile = new File(ctx.getRealPath(".") + PathConfig.COMMENTS_FILE);
		boolean created = commentsFile.createNewFile();
		if (created)
			mapper.writeValue(commentsFile, new ArrayList<Comment>());
		return mapper.readValue(commentsFile, new TypeReference<ArrayList<Comment>>() {
		});
	}

	private ArrayList<Comment> getCommentByApartmentId(String apartmentId)
			throws JsonParseException, JsonMappingException, IOException {
		Apartment apartment = getApartmentById(apartmentId);
		ArrayList<Comment> comments = readComments();
		comments.stream().filter(comment -> apartment.getCommentsId().stream().anyMatch(id -> id == comment.getId()));
		return comments;
	}

	private ArrayList<Amenity> readAmenities() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File amenitiesFile = new File(ctx.getRealPath(".") + PathConfig.AMENITIES_FILE);
		boolean created = amenitiesFile.createNewFile();
		if (created)
			mapper.writeValue(amenitiesFile, new ArrayList<Comment>());
		return mapper.readValue(amenitiesFile, new TypeReference<ArrayList<Amenity>>() {
		});
	}

}
