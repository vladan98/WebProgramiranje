/**
 * 
 */
$(document).ready(function(){
	//Get all reservations for his apartments
	$.ajax({
		type: "GET",
		url: "rest/reservation/host",
		contentType: "application/json",
		success: function(reservations){
			for(let res of reservations){
				addReservationToTable(res)
			}
		},
		error: function(error){
			console.log('error getting reservations')
		}
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
}

function addToTable(apartment, reservation){
	let tr = $('<tr></tr>')
	let tdLocation = $('<td>' + apartment.location.address.street + " " + apartment.location.address.city + " " + apartment.location.address.number +'</td>')
	let tdType = $('<td>' + apartment.apartmentType + '</td>')
	var d = new Date(reservation.startDate);
	let tdStartDate = $('<td>' + d.toString() + '</td>')
	let tdNights = $('<td>' + reservation.nights + '</td>')
	let tdStatus = $('<td>' + reservation.status + '</td>')
	let tdOperation = "<td></td>"
	let tdOperation2 = "<td></td>"
	if(reservation.status == "CREATED" || reservation.status == "ACCEPTED"){
		tdOperation = '<td><a href="#" data-id="' + reservation.id + '"class="accept">Accept resercation</a></td>' 
		tdOperation2 = '<td><a href="#" data-id="' + reservation.id + '"class="decline">Decline resercation</a></td>' 
	}
	let tdOperation3 = '<td><a href="#" data-id="' + reservation.id + '"class="end">End reservation</a></td>' 
	
	

	$(document).on("click", ".accept", function (){
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/reservation/" + id + "/host/accept",
			success:function(){
				alert('Successfully accepted reservation')
				window.location.reload()
			},
			error: function(error){
				console.log(error)
			}
		})
	})
	
	$(document).on("click", ".decline", function (){
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/reservation/" + id +"/host/reject",
			success:function(){
				alert('Successfully rejected reservation')
				window.location.reload()
			},
			error: function(error){
				console.log(error)
			}
		})
	})
	
	$(document).on("click", ".end", function (){
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/reservation/" + id +"/host/end",
			success:function(){
				alert('Successfully ended reservation')
				window.location.reload()
			},
			error: function(error){
				alert('Can not end reservation, its still in progress...')
				console.log(error)
			}
		})
	})
	tr.append(tdLocation).append(tdType).append(tdStartDate).append(tdNights).append(tdStatus).append(tdOperation).append(tdOperation2).append(tdOperation3)
	$('#reservations tbody').append(tr)
}