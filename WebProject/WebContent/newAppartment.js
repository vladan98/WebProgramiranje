$(document).ready(function(){
	//Get all apartments types and amenities
	$.ajax({
		type:"GET",
		url:"rest/amenities",
		contentType:"application/json",
		success:function(amenities){
			for(let amenity of amenities){
				addAmenity(amenity)
			}
		},
		error:function(){
			alert('error getting amenities')
		}
	})

	$('#newApp-form').submit(function(event){
		event.preventDefault()
		let appType = $('#appartmentType').val()
		let roomsNumber = $('#rooms').val()
		let quests = $('#guests').val()
		let street = $('#street').val()
		let number = $('#number').val()
		let city = $('#city').val()
		let postal = $('#postal').val()
		let latitute = $('#latitute').val()
		let longitute = $('#longitute').val()
		let startDate = $('#startDate').val()
		let endDate = $('#endDate').val()
		let price = $('#price').val()
		if(roomsNumber == "0" || quests == "0" || street== "" || city == "" || startDate=="" || endDate ==""){
			alert('Fill fields...')
			return
		}
		//Convert to int
		let postalInt = 0
		if(postal != ""){
			postalInt = parseInt(postal)
		}


		//Create Adress object
		var address = {}
		address= {
			street: street,
			number: number,
			city:   city,
			postal: postalInt
		}

		var location = {
			longitute: longitute,
			latitute: latitute,
			address: adress
		}

		// Check which amenities are selected
		var $boxes = $('input[name=amenities]:checked');
		$boxes.each(function(){
			console.log($(this).val())
		})

		//Get dates included between start and endDate
		var dateFrom = Date.parse(startDate)
		var dateTo = Date.parse(endDate)
		var validDates = getDates(dateFrom, dateTo)
		console.log(validDates)


		$.ajax({
			type:"POST",
			url: "rest/apartment",
			data: JSON.stringify({
				appartmentType: appType,
				rooms: parseint(roomsNumber),
				quests: parseint(quests),
				location: location,
				dates: validDates,
			}),
			contentType:"application/json",
			success: function(){
				alert('crated sucessfully..')
				window.location.href="homepage.html"
			},
			error: function(){
				alert('Some error occurred...')
			}
		})
	})

})

function addAmenity(amenity){
	$('#checkboxes')
    .append(
       $(document.createElement('input')).attr({
           name:  'amenities',
           value:  amenity.id,
           type:  'checkbox'
       })
    .append(
    	$('<label>'+amenity.name+'</label>')
    )
    );
}

Date.prototype.addDays = function(days) {
    var date = new Date(this.valueOf());
    date.setDate(date.getDate() + days);
    return date;
}

function getDates(startDate, stopDate) {
    var dateArray = new Array();
    var currentDate = startDate;
    while (currentDate <= stopDate) {
        dateArray.push(new Date (currentDate));
        currentDate = currentDate.addDays(1);
    }
    return dateArray;
}