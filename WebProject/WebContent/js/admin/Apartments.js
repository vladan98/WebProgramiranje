$(document).ready(() => {
  getApartments();
  getAmenities();
  $("#filter-apartments-form").submit(filterApartments);
});

const getApartments = () => {
  $.ajax({
    type: "GET",
    url: "rest/apartment",
    success: (apartments) => {
       apartmentsTable(apartments);
    },
    error: function () {
      console.log("something bad happened...");
    },
  });
};

const changeStatus = (id) => {
  $.ajax({
    type: "PUT",
    url: `rest/apartment/remove/${id}`,
    success: (users) => {
      getApartments();
    },
    error: function () {
      console.log("something bad happened...");
    },
  });
};

const getComments = (id) => {
  $("#modal-body").empty();

  $.ajax({
    type: "GET",
    url: `rest/comment/apartment/${id}/admin-host`,
    success: (comments) => {
      comments.forEach((comment) => {
        $("#modal-body").append(`<div> ${comment.text} </div>`);
      });
    },
    error: function () {
      console.log("something bad happened...");
    },
  });
};

const getAmenities = () => {
  $.ajax({
    type: "GET",
    url: "rest/amenity",
    contentType: "application/json",
    success: function (amenities) {
      console.log(amenities);
      for (let amenity of amenities) {
        addAmenity(amenity);
      }
    },
    error: function () {
      alert("error getting amenities");
    },
  });
};

const addAmenity = (amenity) => {
  $("#checkboxes").append(`
	<div class="form-check">
			<input type="checkbox" class="form-check-input" id="${amenity.id}" name="amenities" value="${amenity.id}" >
			<label class="form-check-label" for="${amenity.id}">${amenity.name}</label>
	</div>
	`);
};

const addRemoveButton = (id) => {
  return `<button onClick="changeStatus('${id}')" type="button" class="btn btn-info">Remove</button> `;
};

const filterApartments = (event) => {
  event.preventDefault();
  let apartmentType = $("#type").val();
  let startDate = $("#startDate").val();
  let endDate = $("#endDate").val();
  let city = $("#city").val();
  let priceMin = $("#priceMin").val();
  let priceMax = $("#priceMax").val();
  let roomsMin = $("#roomsMin").val();
  let roomsMax = $("#roomsMax").val();
  let guestsMin = $("#guestsMin").val();
  let guestsMax = $("#guestsMax").val();
  let amenities = [];
  let $boxes = $("input[name=amenities]:checked");
  $boxes.each(function () {
    amenities.push($(this).val());
  });
  let sort = $("#sort").val();

  $.ajax({
    type: "POST",
    url: `rest/apartment/filter?sort=${sort}`,
    data: JSON.stringify({
      apartmentType: apartmentType,
      startDate: startDate,
      endDate: endDate,
      city: city,
      priceMin: priceMin,
      priceMax: priceMax,
      roomsMin: roomsMin,
      roomsMax: roomsMax,
      guestsMin: guestsMin,
      guestsMax: guestsMax,
      amenitiesId: amenities,
    }),
    contentType: "application/json",
    success: (apartments) => {

      apartmentsTable(apartments);
    },
    error: function () {
      console.log("something bad happened...");
    },
  });
};

const apartmentsTable = (apartments) =>{
	$("#apartments").empty();
	apartments.forEach((apartment) => {
        $("#apartments").append(
          `<tr>
			   <td> ${apartment.id} </td>
				<td> ${apartment.apartmentType} </td>
				<td> ${apartment.rooms} </td>
				<td> ${apartment.guests} </td>
				<td> <button onClick="getComments('${
          apartment.id
        }')" type="button" class="btn btn-primary" data-toggle="modal" data-target="#exampleModal">
						Comments
					 </button> </td>
				<td> <button class="btn btn-primary"> Amenities </td>
				<td> ${apartment.checkIn} </td>
				<td> ${apartment.checkOut} </td>
			   <td> ${apartment.active == true ? "ACTIVE" : "INACTIVE"} </td>
			   <td> ${apartment.active == true ? addRemoveButton(apartment.id) : ""} </td>
		   </tr>`
        );
      });
}
