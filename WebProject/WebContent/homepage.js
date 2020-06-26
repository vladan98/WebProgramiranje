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
	
	//update profile
	
	//Get all apartments
	$('form#search').submit(function(event){
		event.preventDefault()
		let location = $('#location').val()
		let sorting = $('#sort').val()
		let url ="rest/apartment/search?value="+location+"&sort="+sorting
		console.log(url)
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

