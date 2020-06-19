package services;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import beans.Comment;
import beans.Gender;
import beans.Reservation;
import beans.Role;
import beans.User;
import config.PathConfig;

@Path("/user")
public class UserService {

	@Context
	ServletContext ctx;

	@Context
	HttpServletRequest request;

	@Path(value = "")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response register(User user)
			throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException {

		ArrayList<User> users = readUsers();
		int ind = 0;
		for (User u : users) {
			if (u.getUsername().equals(user.getUsername())) {
				ind = 1;
				break;
			}
		}
		if (ind == 0) {
			User u = new User(user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName(),
					user.getGender(), user.getRole());
			users.add(u);
			writeUsers(users);
			request.getSession().setAttribute("loggedUser", u);
			return Response.status(Response.Status.OK).entity(u).build();
		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	@Path("/login")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(User user) throws IOException, NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(user.getPassword().getBytes());
		String encodedPassword = new String(messageDigest.digest());

		ArrayList<User> users = readUsers();
		for (User u : users) {
			if (u.getUsername().equals(user.getUsername()) && u.getPassword().equals(encodedPassword) && u.isActive()) {
				request.getSession().setAttribute("loggedUser", u);
				return Response.status(Response.Status.OK).entity(u).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@Path("/logout")
	@POST
	public Response logout() {
		request.getSession().invalidate();
		return Response.status(Response.Status.OK).build();
	}

	@Path("")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateProfile(User user)
			throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException {
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		messageDigest.update(user.getPassword().getBytes());
		String encodedPassword = new String(messageDigest.digest());

		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.FORBIDDEN).build();
		if (!loggedUser.getUsername().equals(user.getUsername()))
			return Response.status(Response.Status.UNAUTHORIZED).build();

		ArrayList<User> users = readUsers();
		for (User u : users) {
			if (u.getId().equals(user.getId())) {
				u.setPassword(encodedPassword);
				u.setFirstName(user.getFirstName());
				u.setLastName(user.getLastName());
				u.setUsername(user.getUsername());
				writeUsers(users);
				request.getSession().setAttribute("loggedUser", u);
				return Response.status(Response.Status.OK).entity(u).build();
			}
		}
		return Response.status(Response.Status.NOT_FOUND).build();
	}

	@Path("")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllUsers() throws IOException, NoSuchAlgorithmException {

		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.ADMIN)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<User> users = readUsers();
		return Response.status(Response.Status.OK).entity(users).build();
	}

	@Path("/profile")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser() {
		User user = (User) request.getSession().getAttribute("loggedUser");
		if (user == null) {
			return Response.status(Response.Status.UNAUTHORIZED).build();
		}
		return Response.status(Response.Status.OK).entity(user).build();
	}

	@Path("/change-status")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response changeStatus(User user)
			throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.ADMIN)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<User> users = readUsers();
		for (User u : users) {
			if (u.getId().equals(user.getId())) {
				if (u.isActive()) {
					u.setActive(false);
				} else {
					u.setActive(true);
				}
				writeUsers(users);
				return Response.status(Response.Status.OK).entity(u).build();

			}
		}
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	@GET
	@Path("/search-all/admin/{search}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchUsersAdmin(@PathParam("search") String search) throws IOException, NoSuchAlgorithmException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.ADMIN)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<User> filteredUsers = new ArrayList<User>();
		ArrayList<User> users = readUsers();

		for (User u : users) {
			if (u.getUsername().toLowerCase().contains(search.toLowerCase())
					|| u.getFirstName().toLowerCase().contains(search.toLowerCase())
					|| u.getLastName().toLowerCase().contains(search.toLowerCase()))
				filteredUsers.add(u);
		}
		return Response.status(Response.Status.OK).entity(filteredUsers).build();
	}

	@GET
	@Path("/search-all/host")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchUsersHost(User searchUser) throws NoSuchAlgorithmException, IOException {
		User loggedUser = (User) request.getSession().getAttribute("loggedUser");
		if (loggedUser == null)
			return Response.status(Response.Status.UNAUTHORIZED).build();
		if (loggedUser.getRole() != Role.HOST)
			return Response.status(Response.Status.FORBIDDEN).build();

		ArrayList<User> users = readUsers();
		ArrayList<Reservation> reservations = readReservations();
		ArrayList<Reservation> hostsReservations = reservations.stream().filter(reservation -> {
			try {
				return getApartmentById(reservation.getApartmentId()).getHostId().equals(loggedUser.getId());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}).collect(Collectors.toCollection(ArrayList::new));

		users = users.stream()
				.filter(user -> hostsReservations.stream()
						.anyMatch(reservation -> reservation.getGuestId().equals(user.getId())))
				.collect(Collectors.toCollection(ArrayList::new));

		if (searchUser.getRole() != null)
			users = users.stream().filter(user -> user.getRole() == searchUser.getRole())
					.collect(Collectors.toCollection(ArrayList::new));

		if (searchUser.getGender() != null)
			users = users.stream().filter(user -> user.getGender() == searchUser.getGender())
					.collect(Collectors.toCollection(ArrayList::new));

		if (searchUser.getUsername() != null)
			users = users.stream()
					.filter(user -> user.getUsername().toLowerCase().contains(searchUser.getUsername().toLowerCase()))
					.collect(Collectors.toCollection(ArrayList::new));

		return Response.status(Response.Status.OK).entity(users).build();

	}

	private ArrayList<User> readUsers() throws IOException, NoSuchAlgorithmException {
		ObjectMapper mapper = new ObjectMapper();
		File userFile = new File(ctx.getRealPath(".") + PathConfig.USERS_FILE);
		boolean created = userFile.createNewFile();
		if (created) {
			ArrayList<User> admins = new ArrayList<User>();
			admins.add(new User("admin", "admin", "Admin FirstName", "Admin LastName", Gender.MALE, Role.ADMIN));
			mapper.writeValue(userFile, admins);
		}

		return mapper.readValue(userFile, new TypeReference<ArrayList<User>>() {
		});
	}

	private void writeUsers(ArrayList<User> users) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File userFile = new File(ctx.getRealPath(".") + PathConfig.USERS_FILE);
		userFile.createNewFile();
		mapper.writeValue(userFile, users);
	}

	public User getUserById(String id)
			throws JsonParseException, JsonMappingException, IOException, NoSuchAlgorithmException {
		for (User u : readUsers()) {
			if (u.getId().contentEquals(id))
				return u;
		}
		return null;
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

	private ArrayList<Apartment> readApartments() throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		File apartmentsFile = new File(ctx.getRealPath(".") + PathConfig.APARTMENTS_FILE);
		boolean created = apartmentsFile.createNewFile();
		if (created)
			mapper.writeValue(apartmentsFile, new ArrayList<Apartment>());
		return mapper.readValue(apartmentsFile, new TypeReference<ArrayList<Apartment>>() {
		});
	}

	private Apartment getApartmentById(String id) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Apartment> apartments = readApartments();
		for (Apartment apartment : apartments) {
			if (apartment.getId().equals(id))
				return apartment;
		}
		return null;
	}
}
