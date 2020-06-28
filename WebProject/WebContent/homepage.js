/**
 * 
 */
$(document).ready(function(){
	//Check locale storage 
	var user = JSON.parse(localStorage.getItem('user'));
	console.log(user)
	
	$('#loggedIn').text(user.role+" : " +user.username)
	if(user.role!=0){
		$('#users').hide()
	}
	
	$('#check').text(user.username)
	
	if(user.role === "HOST"){
		$('#mainTable').hide()
		$('#hostActiveTable').show()
		$('#hostInactiveTable').show()
	}else{
		$('#mainTable').show()	
		$('#hostActiveTable').hide()
		$('#hostInactiveTable').hide()
	}
	
	//update profile
	
	//Get all apartments
	$('form#search').submit(function(event){
		event.preventDefault()
		let location = $('#location').val()
		let sorting = $('#sort').val()
		let url = ""
		if(user.role == "HOST"){
			url = "rest/apartment/search/host?value="+location+"&sort="+sorting
			console.log(url)
			SearchAsHost(url, user)
		}else{
			url = "rest/apartment/search?value="+location+"&sort="+sorting
			console.log(url)
			SearchAsGuest(url)
		}
	})
	
})

function addAppartmentToTable(aprt){
	let tr = $('<tr></tr>')
	let th = $('<th scope="row">#</th>')
	let tdImage = $('<td class="w-25"><img src="${aprt.images[0]}" class="img-fluid img-thumbnail" alt="Sheep"></td>')
	let tdType = $('<td>' + aprt.apartmentType +'</td>')
	let tdLoc = $('<td>' + aprt.location.address.street +" " +aprt.location.address.number + " " + aprt.location.address.city  +'</td>')
	let tdRooms = $('<td>' + aprt.rooms + '</td>')
	let tdGuests = $('<td>' + aprt.guests + '</td>')
	let dtprice = $('<td>' + aprt.price + '</td>')
	tr.append(th).append(tdImage).append(tdType).append(tdLoc).append(tdRooms).append(tdGuests).append(dtprice)
	$('#result tbody').append(tr)
	
	tr.click(clickClosure(aprt));
}

function addAppartmentToHostActiveTable(aprt){
	let tr = $('<tr></tr>')
	let th = $('<th scope="row">#</th>')
	let tdImage = $('<td class="w-25"><img src="${aprt.images[0]}" class="img-fluid img-thumbnail" alt="Sheep"></td>')
	let tdType = $('<td>' + aprt.apartmentType +'</td>')
	let tdLoc = $('<td>' + aprt.location.address.street +" " +aprt.location.address.number + " " + aprt.location.address.city  +'</td>')
	let tdRooms = $('<td>' + aprt.rooms + '</td>')
	let tdGuests = $('<td>' + aprt.guests + '</td>')
	let dtprice = $('<td>' + aprt.price + '</td>')
	let tdRemove = $('<td><a href="#" class="izbrisi" data-id="' + aprt.id +'">Remove</a></td>')
	$(document).on("click", ".izbrisi", function (){
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/apartment/remove/" + id,
			success:function(){
				alert('Apartment successfully removed')
				window.location.reload()
			},
			error: function(error){
				console.log(error)
			}
		})
	})
	tr.append(th).append(tdImage).append(tdType).append(tdLoc).append(tdRooms).append(tdGuests).append(dtprice).append(tdRemove)
	$('#hostAp tbody').append(tr)
	
	//tr.click(clickClosure(aprt));
}

function addAppartmentToHostInactiveTable(aprt){
	let tr = $('<tr></tr>')
	let th = $('<th scope="row">#</th>')
	let tdImage = $('<td class="w-25"><img src="${aprt.images[0]}" class="img-fluid img-thumbnail" alt="Sheep"></td>')
	let tdType = $('<td>' + aprt.apartmentType +'</td>')
	let tdLoc = $('<td>' + aprt.location.address.street +" " +aprt.location.address.number + " " + aprt.location.address.city  +'</td>')
	let tdRooms = $('<td>' + aprt.rooms + '</td>')
	let tdGuests = $('<td>' + aprt.guests + '</td>')
	let dtprice = $('<td>' + aprt.price + '</td>')
	let tdActivate = $('<td><a href="#" class="aktiviraj" data-id="' + aprt.id +'">Activate</a></td>')
	$(document).on("click", ".aktiviraj", function () {
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/apartment/activate/" + id,
			success:function(){
				alert('Apartment successfully activated')
				window.location.reload()
			},
			error: function(error){
				console.log(error)
			}
		})
	})
	
	let tdRemove = $('<td><a href="#" class="izbrisi" data-id="' + aprt.id +'">Remove</a></td>')
	
	$(document).on("click", ".izbrisi", function (){
		let id = $(this).attr("data-id")
		//Ajax request for activating apartment
		$.ajax({
			type:"PUT",
			url: "rest/apartment/remove/" + id,
			success:function(){
				alert('Apartment successfully removed')
				window.location.reload()
			},
			error: function(error){
				console.log(error)
			}
		})
	})
	
	tr.append(th).append(tdImage).append(tdType).append(tdLoc).append(tdRooms).append(tdGuests).append(dtprice).append(tdActivate).append(tdRemove)
	$('#hostInactiveAp tbody').append(tr)
	
	//tr.click(clickClosure(aprt));
}

function clickClosure(aprt){
	return function() {
		// Parametar product prosleđen u gornju funkciju će biti vidljiv u ovoj
		// Ovo znači da je funkcija "zapamtila" za koji je apartman vezana
		$('tr.selected').removeClass('selected');
		$(this).addClass('selected');
		console.log(aprt)
		localStorage.wantedApartment = JSON.stringify(aprt)
		window.location.href= "ViewOneApartment.html"
	};
}

function SearchAsGuest(url){
	$.ajax({
		type:"GET",
		url:url,
		contentType:"application/json",
		success:function(apartments){
			for(let ap of apartments){
				addAppartmentToTable(ap)
			}
		},
		error:function(error){
			alert(error)
		}
	})
}

function SearchAsHost(url,user){
	$.ajax({
		type:"GET",
		url:url,
		contentType:"application/json",
		success:function(apartments){
			for(let ap of apartments){
				if(ap.hostId == user.id){
					if(!ap.active){
						addAppartmentToHostInactiveTable(ap)
					}else{
						addAppartmentToHostActiveTable(ap)
					}
				}
			}
		},
		error:function(error){
			alert(error)
		}
	})
}

