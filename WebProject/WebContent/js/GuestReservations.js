/**
 * 
 */

$(document).ready(function(){
	var user = JSON.parse(localStorage.getItem('user'));
	$('#commentForm').hide()
	//Get all users reservations
	$.ajax({
		type: "GET",
		url: "rest/reservation/guest",
		contentType: "application/json",
		success: function(reservations){
			for(let reservation of reservations)
				addReservationToTable(reservation)
		},
		error: function(){
			console.log('error getting reservations')
		}
	})
	
	$('form#commentForm').submit(function(event){
		event.preventDefault()
		let reservation = JSON.parse(localStorage.reservation)
		console.log(reservation)
		localStorage.removeItem("reservation");
		let message = $('#message').val()
		let rate = $('#rateNum').val()
		if(message == "" || rate == ""){
			alert("Please leave rate  and review")
		}
		let reviewerId = user.id
		let apartmentId = reservation.apartmentId
		
		$.ajax({
			type: "POST",
			url: "rest/comment/apartment",
			contentType:"application/json",
			data: JSON.stringify({
				reviewerId: reviewerId,
				apartmentId: apartmentId,
				text: message,
				rate: parseInt(rate)
			}),
			success: function(){
				alert('Successfully left comment!')
				window.location.reload()
			},
			error: function(){
				alert('Something bad happened...')
			}
		})
	})
})

function addReservationToTable(reservation){
	//Get apartment by id for all informations
	$.ajax({
		type:"GET",
		url: "rest/apartment/" + reservation.apartmentId,
		contentType:"application/json",
		success: function(apart){
			addToTable(apart,reservation)
		},
		error: function(error){
			console.log(error)
		}
	})
	//Populate table
	
}

function addToTable(apartment, reservation){
	let tr = $('<tr></tr>')
	let tdLocation = $('<td>' + apartment.location.address.street + " " + apartment.location.address.city + " " + apartment.location.address.number +'</td>')
	let tdType = $('<td>' + apartment.apartmentType + '</td>')
	var d = new Date(reservation.startDate);
	let tdStartDate = $('<td>' + d.toString() + '</td>')
	let tdNights = $('<td>' + reservation.nights + '</td>')
	let tdStatus = $('<td>' + reservation.status + '</td>')
	let tdCancel = $('<td></td>')
	if(reservation.status == "CREATED" || reservation.status == "ACCEPTED"){
		tdCancel = $('<td><a href="#" name="cancel" data-id="' + reservation.id + '" class="cancel">Cancel reservation</a></td>')
	}
	$(document).on("click", ".cancel", function (){
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/reservation/" + id +"/guest/cancel",
			success:function(){
				alert('Successfully canceled reservation')
				window.location.reload()
			},
			error: function(error){
				console.log(error)
			}
		})
	})
	
	let tdleaveComment = $('<td></td>')
	if(reservation.status == "REJECTED" || reservation.status == "ENDED"){
		tdleaveComment = $('<td><button class="btn btn-info" data-id="' + reservation.apartmentId + '">Leave comment</button></td> ')
	}
	
	$(document).on("click",'[class*=info]', function(){
		let id = $(this).attr("data-id")
		console.log(id)
		$('#commentForm').toggle()
		clickClosure(reservation)
	})
	tr.append(tdLocation).append(tdType).append(tdStartDate).append(tdNights).append(tdStatus).append(tdCancel).append(tdleaveComment)
	$('#reservations tbody').append(tr)
}

function clickClosure(reservation){
		// Parametar product prosleđen u gornju funkciju će biti vidljiv u ovoj
		// Ovo znači da je funkcija "zapamtila" za koji je apartman vezana
		$('tr.selected').removeClass('selected');
		$(this).addClass('selected');
		console.log(reservation)
		localStorage.reservation = JSON.stringify(reservation)
//		window.location.href= "ViewOneApartment.html"
}
