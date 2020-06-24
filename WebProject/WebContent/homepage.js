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
})